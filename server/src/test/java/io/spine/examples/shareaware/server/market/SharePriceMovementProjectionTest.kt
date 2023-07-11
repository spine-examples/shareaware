/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.shareaware.server.market

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.extensions.proto.ProtoTruth
import com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly
import io.spine.client.Filters.eq
import io.spine.examples.shareaware.SharePriceMovementId
import io.spine.examples.shareaware.given.GivenMoney.usd
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute
import io.spine.examples.shareaware.server.ProjectionReader
import io.spine.examples.shareaware.server.given.GivenShare.tesla
import io.spine.examples.shareaware.server.market.given.MarketTestEnv.marketSharesUpdated
import io.spine.examples.shareaware.server.market.given.ProjectionActivityTime
import io.spine.examples.shareaware.server.market.given.ShareFieldInProjection
import io.spine.examples.shareaware.server.market.given.actorContext
import io.spine.examples.shareaware.server.market.given.sharePriceMovementPerMinute
import io.spine.server.BoundedContext
import io.spine.server.BoundedContextBuilder
import io.spine.server.integration.ThirdPartyContext
import io.spine.testing.core.given.GivenUserId.newUuid
import java.time.Duration.ofSeconds
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`SharePriceMovement` projection should")
class SharePriceMovementProjectionTest {

    private lateinit var context: BoundedContext

    private lateinit var repository: SharePriceMovementPerMinuteRepository

    private lateinit var marketData: ThirdPartyContext

    private lateinit var reader: ProjectionReader<SharePriceMovementId,
            SharePriceMovementPerMinute>

    @BeforeEach
    fun setUp() {
        repository = SharePriceMovementPerMinuteRepository()
        context = BoundedContextBuilder
            .assumingTests()
            .add(repository)
            .build()
        marketData = ThirdPartyContext.singleTenant("MarketData")
        reader = ProjectionReader(context.stand(), SharePriceMovementPerMinute::class.java)
    }

    @Test
    @DisplayName("accept the events only for the activity time")
    fun createProjections() {
        val shareId = tesla().id

        marketData.emittedEvent(marketSharesUpdated(), newUuid())
        sleepUninterruptibly(ofSeconds(ProjectionActivityTime))
        val projectionsAfterFirstEmit = reader.read(
            actorContext(),
            eq(ShareFieldInProjection, shareId)
        )
        assertThat(projectionsAfterFirstEmit.size).isEqualTo(1)

        marketData.emittedEvent(marketSharesUpdated(), newUuid())
        sleepUninterruptibly(ofSeconds(ProjectionActivityTime))
        val projectionsAfterSecondEmit = reader.read(
            actorContext(),
            eq(ShareFieldInProjection, shareId)
        )
        assertThat(projectionsAfterSecondEmit.size).isEqualTo(2)

        assertThat(projectionsAfterSecondEmit[0]).isNotEqualTo(projectionsAfterSecondEmit[1])
        assertThat(projectionsAfterSecondEmit[0].movementPointCount).isEqualTo(1)
        assertThat(projectionsAfterSecondEmit[1].movementPointCount).isEqualTo(1)
    }

    @Test
    @DisplayName("construct the `MovementPoint`s from the `MarketSharesUpdate` event")
    fun state() {
        val shareId = tesla().id
        val shareWithLowerPrice = tesla(usd(10))
        val shareWithHigherPrice = tesla(usd(20))
        val eventWithLowerPrice = marketSharesUpdated(shareWithLowerPrice)
        val eventWithHigherPrice = marketSharesUpdated(shareWithHigherPrice)

        marketData.emittedEvent(eventWithLowerPrice, newUuid())
        marketData.emittedEvent(eventWithHigherPrice, newUuid())
        sleepUninterruptibly(ofSeconds(ProjectionActivityTime))
        val projection = reader.read(actorContext(), eq(ShareFieldInProjection, shareId))[0]
        val expectedProjection = sharePriceMovementPerMinute(
            shareId,
            shareWithLowerPrice.price,
            eventWithLowerPrice.whenUpdated,
            shareWithHigherPrice.price,
            eventWithHigherPrice.whenUpdated
        )

        ProtoTruth.assertThat(projection)
            .comparingExpectedFieldsOnly()
            .isEqualTo(expectedProjection)
    }
}
