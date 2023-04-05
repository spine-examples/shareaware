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

import io.spine.examples.shareaware.MarketId;
import io.spine.examples.shareaware.market.Market;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.market.event.SharesObtained;
import io.spine.examples.shareaware.market.event.SharesSoldOnMarket;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;

import java.security.SecureRandom;
import java.util.Random;

import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.*;

/**
 * The imitation of the shares market.
 */
public final class MarketProcess
        extends ProcessManager<MarketId, Market, Market.Builder> {

    /**
     * The hardcoded ID for the shares market.
     *
     * <p>Any signals that match this ID will be directed
     * to the only one instance of this market imitation
     * that can exist in context.
     */
    public static final MarketId ID = MarketId
            .newBuilder()
            .setUuid("ImitationOfSharesMarket")
            .vBuild();

    private static final int MIN_PRICE = 1;
    private static final int MAX_PRICE = 200;

    /**
     * Obtains the requested shares from the market
     * emitting the {@code SharesObtained} event.
     */
    @Assign
    SharesObtained on(ObtainShares c) {
        return SharesObtained
                .newBuilder()
                .setMarket(c.getMarket())
                .setPurchaseProcess(c.getPurchase())
                .setShare(c.getShare())
                .setQuantity(c.getQuantity())
                .vBuild();
    }

    /**
     * Sells wanted shares on the market
     * emitting the {@code SharesSold} event.
     */
    @Assign
    SharesSoldOnMarket on(SellSharesOnMarket c) {
        Money sellPrice = multiply(c.getPrice(), c.getQuantity());
        return SharesSoldOnMarket
                .newBuilder()
                .setMarket(c.getMarket())
                .setSaleProcess(c.getSaleProcess())
                .setShare(c.getShare())
                .setQuantity(c.getQuantity())
                .setPrice(sellPrice)
                .vBuild();
    }
}
