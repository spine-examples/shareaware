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

import io.spine.examples.shareaware.share.Share;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.shareaware.server.market.given.MarketTestEnv.shareWithoutPrice;

@DisplayName("`MarketData` should")
final class MarketDataTest extends UtilityClassTest<MarketData> {

    MarketDataTest() {
        super(MarketData.class);
    }

    @Test
    @DisplayName("expose a constant set of available shared, with their pricing slightly changing upon each update")
    void actualizeShare() {
        List<Share> firstResult = MarketData.actualShares();
        List<Share> secondResult = MarketData.actualShares();
        assertThat(firstResult).hasSize(secondResult.size());

        for (var i = 0; i < firstResult.size(); i++) {
            var shareFromFirst = firstResult.get(i);
            var shareFromSecond = secondResult.get(i);
            var shareFromFirstWithoutPrice = shareWithoutPrice(shareFromFirst);
            var shareFromSecondWithoutPrice = shareWithoutPrice(shareFromSecond);
            assertThat(shareFromFirstWithoutPrice).isEqualTo(shareFromSecondWithoutPrice);
        }
    }
}
