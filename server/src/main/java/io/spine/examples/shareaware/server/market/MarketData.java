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

import com.google.common.collect.ImmutableList;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.share.SharesReader;
import io.spine.money.Money;

import java.io.File;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

/**
 * Provides the currently available shares on the market.
 *
 * <p>This is a simulation of how the real share market operates,
 * focusing on changes in share prices.
 */
final class MarketData {

    private static final Set<Share> shares;

    /**
     * Prevents instantiation of this class.
     */
    private MarketData() {
    }

    static {
        var classLoader = currentThread().getContextClassLoader();
        var urlToFile = requireNonNull(classLoader.getResource("shares.yml"));
        var file = new File(urlToFile.getFile());
        shares = SharesReader.read(file);
    }

    /**
     * Returns the list of up-to-date shares that are available on the market.
     */
    static ImmutableList<Share> actualShares() {
        var actualShares = shares
                .stream()
                .map(MarketData::actualize)
                .collect(Collectors.toList());
        return ImmutableList.copyOf(actualShares);
    }

    /**
     * Actualize the share price.
     *
     * <p>Simulates the share price updates on the market.
     */
    private static Share actualize(Share share) {
        var updatedPrice = updatePrice(share.getPrice());
        return share
                .toBuilder()
                .setPrice(updatedPrice)
                .vBuild();
    }

    private static Money updatePrice(Money previousPrice) {
        var nanosInUnit = 100;
        Random random = new SecureRandom();
        var valueToSumWithUnits = random.nextInt(21) - 10;
        var updatedUnits = previousPrice.getUnits() + valueToSumWithUnits;
        var valueToSumWithNanos = random.nextInt(nanosInUnit) - 50;
        var updatedNanos = previousPrice.getNanos() + valueToSumWithNanos;
        if (updatedNanos / nanosInUnit >= 1) {
            updatedUnits++;
            updatedNanos -= nanosInUnit;
        }
        if (updatedNanos < 0) {
            updatedUnits--;
            updatedNanos += nanosInUnit;
        }
        return previousPrice
                .toBuilder()
                .setUnits(updatedUnits)
                .setNanos(updatedNanos)
                .vBuild();
    }
}
