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

package io.spine.examples.shareaware.server.market.given;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.spine.base.EntityColumn;
import io.spine.core.ActorContext;
import io.spine.core.TenantId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.SharePriceMovementId;
import io.spine.examples.shareaware.market.PriceAtTime;
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.examples.shareaware.share.Share;
import io.spine.money.Money;

import java.util.List;
import java.util.Optional;

import static io.spine.base.Time.currentTime;
import static io.spine.testing.core.given.GivenUserId.newUuid;
import static io.spine.util.Exceptions.newIllegalArgumentException;

public final class SharesPriceMovementTestEnv {

    public static final long ProjectionActivityTime = 60;

    public static final EntityColumn ShareFieldInProjection =
            SharePriceMovementPerMinute.Column.share();

    /**
     * Prevents instantiation of this class.
     */
    private SharesPriceMovementTestEnv() {
    }

    public static SharePriceMovementPerMinute sharePriceMovementPerMinute(
            ShareId shareId,
            MarketSharesUpdated firstEvent,
            MarketSharesUpdated secondEvent
    ) {
        Money firstPrice = retrieveShare(firstEvent.getShareList(), shareId).getPrice();
        Money secondPrice = retrieveShare(secondEvent.getShareList(), shareId).getPrice();
        PriceAtTime firstPriceAtTime = priceAtTime(firstPrice, firstEvent.getWhenUpdated());
        PriceAtTime secondPriceAtTime = priceAtTime(secondPrice, secondEvent.getWhenUpdated());
        Duration activityTime = Duration
                .newBuilder()
                .setSeconds(ProjectionActivityTime)
                .build();
        SharePriceMovementId sharePriceMovementId = sharePriceMovementId(shareId, activityTime);
        return SharePriceMovementPerMinute
                .newBuilder()
                .setId(sharePriceMovementId)
                .addPriceAtTime(firstPriceAtTime)
                .addPriceAtTime(secondPriceAtTime)
                .setShare(shareId)
                .buildPartial();
    }

    public static ActorContext actorContext() {
        TenantId tenantId = TenantId
                .newBuilder()
                .setValue("SharePriceMovementTest")
                .build();
        return ActorContext
                .newBuilder()
                .setActor(newUuid())
                .setTenantId(tenantId)
                .setTimestamp(currentTime())
                .vBuild();
    }

    private static PriceAtTime priceAtTime(Money price, Timestamp time) {
        return PriceAtTime
                .newBuilder()
                .setPrice(price)
                .setTime(time)
                .vBuild();
    }

    private static SharePriceMovementId sharePriceMovementId(
            ShareId share,
            Duration activityTime
    ) {
        return SharePriceMovementId
                .newBuilder()
                .setShare(share)
                .setActivityTime(activityTime)
                .buildPartial();
    }

    private static Share retrieveShare(List<Share> shares, ShareId id) {
        Optional<Share> optionalShare = shares.stream()
                .filter(share -> share.getId()
                                      .equals(id))
                .findAny();
        if (optionalShare.isEmpty()) {
            throw newIllegalArgumentException("There is no share with provided ID in the list.");
        }
        return optionalShare.get();
    }
}
