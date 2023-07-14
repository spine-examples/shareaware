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

package io.spine.examples.shareaware.server.market;

import com.google.common.truth.extensions.proto.ProtoTruth;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.examples.shareaware.server.ProjectionReader;
import io.spine.examples.shareaware.share.Share;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.integration.ThirdPartyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.spine.client.Filters.eq;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.given.GivenShare.tesla;
import static io.spine.examples.shareaware.server.market.given.MarketTestEnv.marketSharesUpdated;
import static io.spine.examples.shareaware.server.market.given.SharesPriceMovementTestEnv.ProjectionActivityTime;
import static io.spine.examples.shareaware.server.market.given.SharesPriceMovementTestEnv.ShareFieldInProjection;
import static io.spine.examples.shareaware.server.market.given.SharesPriceMovementTestEnv.actorContext;
import static io.spine.examples.shareaware.server.market.given.SharesPriceMovementTestEnv.sharePriceMovementPerMinute;
import static io.spine.testing.core.given.GivenUserId.newUuid;
import static java.time.Duration.ofSeconds;

@DisplayName("`SharePriceMovement` projection should")
final class SharePriceMovementProjectionTest {

    private ThirdPartyContext marketData;

    private ProjectionReader<SharePriceMovementPerMinute> reader;

    @BeforeEach
    void setUp() {
        var repository = new SharePriceMovementPerMinuteRepository();
        var context = BoundedContextBuilder
                .assumingTests()
                .add(repository)
                .build();
        marketData = ThirdPartyContext.singleTenant("MarketData");
        reader = new ProjectionReader<>(context.stand(), SharePriceMovementPerMinute.class);
    }

    @Test
    @DisplayName("accept the events only for the activity time")
    void createProjections() {
        var shareId = tesla().getId();

        marketData.emittedEvent(marketSharesUpdated(), newUuid());
        sleepUninterruptibly(ofSeconds(60));
        var projectionsAfterFirstEmit = reader.read(
                actorContext(),
                eq(ShareFieldInProjection, shareId)
        );
        assertThat(projectionsAfterFirstEmit.size()).isEqualTo(1);

        marketData.emittedEvent(marketSharesUpdated(), newUuid());
        sleepUninterruptibly(ofSeconds(ProjectionActivityTime));
        var projectionsAfterSecondEmit = reader.read(
                actorContext(),
                eq(ShareFieldInProjection, shareId)
        );
        assertThat(projectionsAfterSecondEmit.size()).isEqualTo(2);

        assertThat(projectionsAfterSecondEmit.get(0))
                .isNotEqualTo(projectionsAfterSecondEmit.get(1));
        assertThat(projectionsAfterSecondEmit.get(0)
                                             .getPointCount())
                .isEqualTo(1);
        assertThat(projectionsAfterSecondEmit.get(1)
                                             .getPointCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("construct the `PriceAtTime` from the `MarketSharesUpdate` event")
    void state() {
        var shareId = tesla().getId();
        var shareWithLowerPrice = tesla(usd(10));
        var shareWithHigherPrice = tesla(usd(20));
        var eventWithLowerPrice = marketSharesUpdated(shareWithLowerPrice);
        var eventWithHigherPrice = marketSharesUpdated(shareWithHigherPrice);

        marketData.emittedEvent(eventWithLowerPrice, newUuid());
        marketData.emittedEvent(eventWithHigherPrice, newUuid());
        sleepUninterruptibly(ofSeconds(ProjectionActivityTime));
        var projection = reader
                .read(actorContext(), eq(ShareFieldInProjection, shareId))
                .get(0);
        var expectedProjection =
                sharePriceMovementPerMinute(shareId, eventWithLowerPrice, eventWithHigherPrice);

        ProtoTruth.assertThat(projection)
                  .comparingExpectedFieldsOnly()
                  .isEqualTo(expectedProjection);
    }
}
