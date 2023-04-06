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

import io.spine.examples.shareaware.market.Market;
import io.spine.examples.shareaware.market.command.CloseMarket;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.market.command.OpenMarket;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.market.event.MarketClosed;
import io.spine.examples.shareaware.market.event.MarketOpened;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeSoldOnMarket;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeObtained;
import io.spine.examples.shareaware.server.market.given.MarketTestContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import io.spine.testing.server.model.ModelTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.market.given.MarketTestEnv.*;

@DisplayName("`Market` should")
public final class MarketProcessTest extends ContextAwareTest {

    @BeforeAll
    static void beforeAll() {
        ModelTests.dropAllModels();
    }

    @AfterAll
    static void afterAll() {
        ModelTests.dropAllModels();
    }

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return MarketTestContext.newBuilder();
    }

    @Test
    @DisplayName("change state after `OpenMarket` command")
    void stateWhenOpened() {
        CloseMarket commandToClose = closeMarket();
        context().receivesCommand(commandToClose);

        OpenMarket commandToOpen = openMarket();
        context().receivesCommand(commandToOpen);
        Market expected = marketAfter(commandToOpen);

        context().assertState(commandToOpen.getMarket(), expected);
    }

    @Test
    @DisplayName("change state after `CloseMarket` command")
    void stateWhenClosed() {
        CloseMarket command = closeMarket();
        context().receivesCommand(command);
        Market expected = marketAfter(command);

        context().assertState(command.getMarket(), expected);
    }

    @Test
    @DisplayName("emit the `MarketOpened` event")
    void marketOpened() {
        OpenMarket command = openMarket();
        context().receivesCommand(command);
        MarketOpened expected = marketOpenedAfter(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("emit the `MarketClosed` event")
    void marketClosed() {
        CloseMarket command = closeMarket();
        context().receivesCommand(command);
        MarketClosed expected = marketClosedAfter(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("throw rejection to the response for the " +
            "`ObtainShares` command when the market is closed")
    void sharesCannotBeObtained() {
        CloseMarket commandToCloseMarket = closeMarket();
        context().receivesCommand(commandToCloseMarket);

        ObtainShares commandToObtainShares = obtainShares();
        context().receivesCommand(commandToObtainShares);
        SharesCannotBeObtained expected =
                sharesCannotBeObtainedCausedBy(commandToObtainShares);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("throw rejection to the response for the " +
            "`SellSharesOnMarket` command when the market is closed")
    void sharesCannotBeSoldOnMarket() {
        CloseMarket commandToCloseMarket = closeMarket();
        context().receivesCommand(commandToCloseMarket);

        SellSharesOnMarket commandToSellShares = sellSharesOnMarket();
        context().receivesCommand(commandToSellShares);
        SharesCannotBeSoldOnMarket expected =
                sharesCannotBeSoldOnMarketCausedBy(commandToSellShares);

        context().assertEvent(expected);
    }
}
