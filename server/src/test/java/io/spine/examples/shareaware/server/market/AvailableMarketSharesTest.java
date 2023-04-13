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

import com.google.common.truth.Truth;
import io.spine.server.BoundedContext;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.ServerEnvironment;
import io.spine.server.integration.ThirdPartyContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.market.given.MarketTestEnv.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("`AvailableMarketShares` should")
class AvailableMarketSharesTest {

    private BoundedContext context;

    private AvailableMarketSharesRepository repository;

    @BeforeEach
    void prepareContext() {
        repository = new AvailableMarketSharesRepository();
        context = BoundedContextBuilder
                .assumingTests()
                .add(repository)
                .build();
    }

    @AfterEach
    void closeContext() throws Exception {
        context.close();
        ServerEnvironment
                .instance()
                .reset();
    }

    @Test
    @DisplayName("subscribe to the external event `MarketSharesUpdated` and update its state")
    void state() {
        MarketSharesUpdated event = marketSharesUpdated();
        postEventFromThirdPartyContext(event);
        AvailableMarketShares expected = availableMarketSharesAfter(event);

        AvailableMarketSharesProjection projection = repository
                .find(MarketProcess.ID)
                .orElseGet(Assertions::fail);
        Truth.assertThat(projection.state()).isEqualTo(expected);
    }

    private static void postEventFromThirdPartyContext(MarketSharesUpdated event) {
        try (ThirdPartyContext context = ThirdPartyContext.singleTenant("MarketData")) {
            context.emittedEvent(event, actor());
        } catch (Exception e) {
            fail();
        }
    }
}
