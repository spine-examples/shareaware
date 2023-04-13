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
import io.spine.examples.shareaware.Share;
import io.spine.money.Money;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import static io.spine.examples.shareaware.server.market.SharesStore.*;

/**
 * Provides the currently available shares on the market.
 */
class MarketData {

    /**
     * Prevents instantiation of this class.
     */
    private MarketData() {
    }

    /**
     * Returns the list of up-to-date shares that available on the market.
     */
    static List<Share> actualShares() {
        return ImmutableList.of(actualize(apple()),
                                actualize(tesla()),
                                actualize(meta()));
    }

    /**
     * Actualize the share price.
     *
     * <p>Simulates the share price update on the market.
     */
    private static Share actualize(Share share) {
        Random random = new SecureRandom();
        int valueToSummarize = random.nextInt(21) - 10;
        Money previousPrice = share.getPrice();
        Money updatedPrice = previousPrice
                .toBuilder()
                .setUnits(previousPrice.getUnits() + valueToSummarize)
                .vBuild();
        return share
                .toBuilder()
                .setPrice(updatedPrice)
                .vBuild();
    }
}
