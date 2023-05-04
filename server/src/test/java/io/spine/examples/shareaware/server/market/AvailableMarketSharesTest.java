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

import io.spine.examples.shareaware.server.FreshContextTest;
import io.spine.examples.shareaware.server.market.given.MarketTestContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.integration.ThirdPartyContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.market.given.MarketTestEnv.availableMarketSharesAfter;
import static io.spine.examples.shareaware.server.market.given.MarketTestEnv.marketSharesUpdated;
import static io.spine.testing.core.given.GivenUserId.newUuid;

@DisplayName("`AvailableMarketShares` should")
final class AvailableMarketSharesTest extends FreshContextTest {

    private ThirdPartyContext marketData;

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return MarketTestContext.newBuilder();
    }

    @BeforeEach
    void prepareContext() {
        marketData = ThirdPartyContext.singleTenant("MarketData");
    }

    @AfterEach
    void terminateContext() throws Exception {
        marketData.close();
    }

    @Test
    @DisplayName("subscribe to the external event `MarketSharesUpdated` and update its state")
    void state() {
        var event = marketSharesUpdated();
        marketData.emittedEvent(event, newUuid());
        var expected = availableMarketSharesAfter(event);

        context().assertState(MarketProcess.ID, expected);
    }
}
