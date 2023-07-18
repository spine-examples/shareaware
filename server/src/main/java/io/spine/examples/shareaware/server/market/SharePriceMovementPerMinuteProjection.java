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

import io.spine.core.External;
import io.spine.core.Subscribe;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.SharePriceMovementId;
import io.spine.examples.shareaware.market.PriceAtTime;
import io.spine.examples.shareaware.market.SharePriceMovementPerMinute;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.examples.shareaware.share.Share;
import io.spine.money.Money;
import io.spine.server.projection.Projection;

import java.util.Collection;

import static io.spine.util.Exceptions.newIllegalArgumentException;
import static java.lang.String.format;

/**
 * The view of the share price movements per minute.
 */
final class SharePriceMovementPerMinuteProjection
        extends Projection<SharePriceMovementId,
                           SharePriceMovementPerMinute,
                           SharePriceMovementPerMinute.Builder> {

    @Subscribe
    void on(@External MarketSharesUpdated e) {
        var shareId = builder()
                .getId()
                .getShare();
        var price = retrieveSharePrice(e.getShareList(), shareId);
        var priceAtTime = PriceAtTime
                .newBuilder()
                .setPrice(price)
                .setWhen(e.getWhenUpdated())
                .vBuild();
        builder().setShare(shareId)
                 .addPoint(priceAtTime);
    }

    /**
     * Retrieves the share with provided ID from the provided {@code Collection}.
     */
    private static Money retrieveSharePrice(Collection<Share> shares, ShareId id) {
        var optionalShare = shares
                .stream()
                .filter(share -> share.getId().equals(id))
                .findAny();
        if (optionalShare.isEmpty()) {
            String errorMessage =
                    format("There is no share with provided ID - %s in the list - %s.", id, shares);
            throw newIllegalArgumentException(errorMessage);
        }
        return optionalShare.get()
                            .getPrice();
    }
}
