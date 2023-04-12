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

import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.ShareId;
import io.spine.money.Currency;
import io.spine.money.Money;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.util.Collections.*;

class MarketData {

    private static final ShareId appleID = ShareId.generate();

    private static final ShareId teslaID = ShareId.generate();

    private static final ShareId facebookID = ShareId.generate();

    private static final Collection<Share> shares = new ArrayList<>();

    static {
        shares.add(apple());
        shares.add(tesla());
        shares.add(facebook());
    }

    /**
     * Prevents instantiation of this class.
     */
    private MarketData() {
    }

    static List<Share> actualShares() {
        List<Share> updatedShares = shares.stream()
                .map(MarketData::changePrice)
                .collect(Collectors.toList());
        return unmodifiableList(updatedShares);
    }

    private static Share changePrice(Share share) {
        Random random = new SecureRandom();
        int randomNumber = random.nextInt(21) - 10;
        Money previousPrice = share.getPrice();
        long updatedPrice = previousPrice.getUnits() + randomNumber;
        return share
                .toBuilder()
                .setPrice(usd(updatedPrice))
                .vBuild();
    }

    private static Share apple() {
        return Share
                .newBuilder()
                .setId(appleID)
                .setPrice(usd(200))
                .vBuild();
    }

    private static Share tesla() {
        return Share
                .newBuilder()
                .setId(teslaID)
                .setPrice(usd(300))
                .vBuild();
    }

    private static Share facebook() {
        return Share
                .newBuilder()
                .setId(facebookID)
                .setPrice(usd(150))
                .vBuild();
    }

    private static Money usd(long units) {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(units)
                .vBuild();
    }
}
