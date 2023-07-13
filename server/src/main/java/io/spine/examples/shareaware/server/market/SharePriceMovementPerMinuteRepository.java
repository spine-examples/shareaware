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

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.spine.examples.shareaware.SharePriceMovementId;
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRouting;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static io.spine.base.Time.currentTime;

/**
 * Manages instances of the {@link SharePriceMovementPerMinuteProjection}.
 */
public final class SharePriceMovementPerMinuteRepository extends
                                             ProjectionRepository<SharePriceMovementId,
                                                     SharePriceMovementPerMinuteProjection,
                                                     SharePriceMovementPerMinute> {

    private static final int SECONDS_IN_MINUTE = 60;

    private static final Duration PROJECTION_ACTIVE_TIME = Duration
            .newBuilder()
            .setSeconds(SECONDS_IN_MINUTE)
            .build();

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<SharePriceMovementId> routing) {
        super.setupEventRouting(routing);
        routing.route(MarketSharesUpdated.class, (event, context) -> toSharePriceMovements(event));
    }

    /**
     * Routes the {@code MarketSharesUpdated} event to the {@code SharePriceMovementPerMinute} projections.
     */
    private static ImmutableSet<SharePriceMovementId>
    toSharePriceMovements(MarketSharesUpdated event) {
        return event.getShareList()
                .stream()
                .map(share -> {
                    Timestamp whenCreated = roundDownToNearestMinute(currentTime());
                    return SharePriceMovementId
                            .newBuilder()
                            .setShare(share.getId())
                            .setActivityTime(PROJECTION_ACTIVE_TIME)
                            .setWhenCreated(whenCreated)
                            .vBuild();
                })
                .collect(toImmutableSet());
    }

    /**
     * Rounds the provided {@code Timestamp} down to the nearest minute.
     */
    private static Timestamp roundDownToNearestMinute(Timestamp timestamp) {
        long seconds = timestamp.getSeconds() - timestamp.getSeconds() % SECONDS_IN_MINUTE;
        return Timestamp
                .newBuilder()
                .setSeconds(seconds)
                .setNanos(0)
                .build();
    }
}