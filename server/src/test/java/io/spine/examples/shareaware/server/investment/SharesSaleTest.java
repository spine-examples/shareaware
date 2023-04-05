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

package io.spine.examples.shareaware.server.investment;

import io.spine.examples.shareaware.SaleId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.command.SellShares;
import io.spine.examples.shareaware.investment.event.SharesSold;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.given.GivenMoney;
import io.spine.examples.shareaware.server.given.WalletTestEnv;
import io.spine.examples.shareaware.server.investment.given.InvestmentTestContext;
import io.spine.examples.shareaware.server.investment.given.RejectingMarket;
import io.spine.examples.shareaware.server.investment.given.SharesSaleTestEnv;
import io.spine.examples.shareaware.server.wallet.MoneyCalculator;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.money.Money;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.investment.given.InvestmentTestEnv.purchaseSharesFor;
import static io.spine.examples.shareaware.server.investment.given.SharesSaleTestEnv.*;
import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.*;

public class SharesSaleTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return InvestmentTestContext.newBuilder();
    }

    @Nested
    @DisplayName("recharge the wallet balance")
    class RechargeBalance {

        @Test
        void state() {
            Wallet wallet = WalletTestEnv.setUpReplenishedWallet(context());
            PurchaseShares purchase = purchaseSharesFor(wallet.getId()
                                                              .getOwner());
            SellShares command = sellShareAfter(purchase);
            Money currentBalance = subtract(wallet.getBalance(), purchase.getSharePrice());
            Wallet expected = wallet
                    .toBuilder()
                    .setBalance(sum(currentBalance, RejectingMarket.pricePerShare))
                    .vBuild();
            context().receivesCommand(command);

            context().assertState(wallet.getId(), expected);
        }
    }
}
