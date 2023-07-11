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

import com.google.common.collect.ImmutableSet
import com.google.common.collect.ImmutableSet.toImmutableSet
import com.google.protobuf.Duration
import io.spine.base.Time.currentTime
import io.spine.client.Filters.eq
import io.spine.core.ActorContext
import io.spine.examples.shareaware.ShareId
import io.spine.examples.shareaware.SharePriceMovementId
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute
import io.spine.examples.shareaware.market.event.MarketSharesUpdated
import io.spine.examples.shareaware.server.ProjectionReader
import io.spine.examples.shareaware.share.Share
import io.spine.server.projection.ProjectionRepository
import io.spine.server.route.EventRouting

/**
 * Manages instances of the [SharePriceMovementPerMinuteProjection].
 */
public class SharePriceMovementPerMinuteRepository :
    ProjectionRepository<SharePriceMovementId,
            SharePriceMovementPerMinuteProjection,
            SharePriceMovementPerMinute>() {

    private val sharePriceMovementActivityTime: Long = 60

    override fun setupEventRouting(routing: EventRouting<SharePriceMovementId>) {
        super.setupEventRouting(routing)
        routing.route(MarketSharesUpdated::class.java) { event, context ->
            toSharePriceMovements(event, context.actorContext())
        }
    }

    /**
     * Routes the `MarketSharesUpdated` event to the `SharePriceMovementPerMinute` projections.
     */
    private fun toSharePriceMovements(
        event: MarketSharesUpdated, context: ActorContext
    ): ImmutableSet<SharePriceMovementId> {
        val reader = ProjectionReader(
            context().stand(),
            SharePriceMovementPerMinute::class.java
        )
        val shareField = SharePriceMovementPerMinute.Field.share()
        return event.shareList.stream().map { share: Share ->
            val sharePriceMovements = reader.read(context, eq(shareField, share.id))
            if (sharePriceMovements.isNotEmpty()) {
                findActiveOrCreate(sharePriceMovements)
            } else {
                createNewSharePriceMovementId(share.id)
            }
        }.collect(toImmutableSet())
    }

    /**
     * Finds the ID of the `SharesPriceMovementPerMinute` projection which activity time
     * has not yet expired, and it is still collecting data about share price movements.
     *
     * Whether the ID of the active projection is not found the new ID will be returned.
     */
    private fun findActiveOrCreate(
        sharePriceMovements: List<SharePriceMovementPerMinute>
    ): SharePriceMovementId {
        val activeProjection = sharePriceMovements
            .find { priceMovement -> priceMovement.isActive() }
            ?: return createNewSharePriceMovementId(sharePriceMovements[0].share)
        return activeProjection.id
    }

    /**
     * Determines whether is the `SharePriceMovementPerMinute` projection still
     * collecting data about share price movements or not.
     */
    private fun SharePriceMovementPerMinute.isActive(): Boolean {
        val timeFromCreation = currentTime().minus(this.id.whenCreated)
        return this.id.activityTime.greaterThan(timeFromCreation)
    }

    /**
     * Returns the new ID for the `SharePriceMovementPerMinute` taking the ID of the share.
     */
    private fun createNewSharePriceMovementId(share: ShareId): SharePriceMovementId {
        val duration = Duration
            .newBuilder()
            .setSeconds(sharePriceMovementActivityTime)
            .build()
        return SharePriceMovementId
            .newBuilder()
            .setShare(share)
            .setActivityTime(duration)
            .setWhenCreated(currentTime())
            .vBuild()
    }
}
