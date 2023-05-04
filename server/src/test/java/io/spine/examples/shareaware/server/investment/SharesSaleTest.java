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

import io.spine.examples.shareaware.investment.command.CancelSharesReservation;
import io.spine.examples.shareaware.investment.command.CompleteSharesReservation;
import io.spine.examples.shareaware.investment.command.ReserveShares;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.server.FreshContextTest;
import io.spine.examples.shareaware.server.investment.given.InvestmentTestContext;
import io.spine.examples.shareaware.server.investment.given.RejectingMarket;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.server.BoundedContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.setUpReplenishedWallet;
import static io.spine.examples.shareaware.server.investment.given.InvestmentTestEnv.*;
import static io.spine.examples.shareaware.server.investment.given.SharesSaleTestEnv.*;

@DisplayName("`SharesSale` should")
public class SharesSaleTest extends FreshContextTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return InvestmentTestContext.newBuilder();
    }

    @Nested
    @DisplayName("recharge the wallet balance")
    class RechargeWallet {

        @Test
        @DisplayName("for the sell price")
        void state() {
            var wallet = setUpReplenishedWallet(context());
            var purchaseCommand = purchaseShares(wallet);
            context().receivesCommand(purchaseCommand);

            var sellCommand = sellShareAfter(purchaseCommand);
            context().receivesCommand(sellCommand);
            var expected = walletAfter(purchaseCommand, sellCommand, wallet);

            context().assertState(wallet.getId(), expected);
        }

        @Test
        @DisplayName("emitting the `BalanceRecharged` event")
        void event() {
            var wallet = setUpReplenishedWallet(context());
            var purchaseCommand = purchaseShares(wallet);
            context().receivesCommand(purchaseCommand);

            var sellCommand = sellShareAfter(purchaseCommand);
            context().receivesCommand(sellCommand);
            var expected = balanceRechargedAfter(sellCommand, purchaseCommand, wallet);

            context().assertEvents()
                     .withType(BalanceRecharged.class)
                     .message(1)
                     .isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("interact with `Investment`")
    class InvestmentBehaviour {

        @Test
        @DisplayName("by reducing the number of available shares")
        void state() {
            var investment = setUpInvestment(context());
            var command = sellShareFrom(investment);
            context().receivesCommand(command);
            var expected = investmentAfter(command, investment);

            context().assertState(investment.getId(), expected);
        }

        @Test
        @DisplayName("by emitting the `SharesReserved` event")
        void sharesReserved() {
            var investment = setUpInvestment(context());
            var command = sellShareFrom(investment);
            context().receivesCommand(command);
            var expected = sharesReservedBy(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("by emitting the `SharesReservationCompleted` event")
        void sharesReservationCompleted() {
            var investment = setUpInvestment(context());
            var command = sellShareFrom(investment);
            context().receivesCommand(command);
            var expected = sharesReservationCompletedBy(command, investment);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("by emitting the `SharesReservationCanceled event " +
                "after error in the shares market")
        void sharesReservationCanceled() {
            var investment = setUpInvestment(context());
            var command = sellShareFrom(investment);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);
            var expected = sharesReservationCanceledAfter(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }

        @Test
        @DisplayName("by emitting the `InsufficientShares` rejection")
        void insufficientShares() {
            var investment = setUpInvestment(context());
            var command = sellMoreSharesThanIn(investment);
            context().receivesCommand(command);
            var expected = insufficientSharesCausedBy(command);

            context().assertEvent(expected);
        }
    }

    @Test
    @DisplayName("reduce the number of available shares in the `InvestmentView` projection")
    void projection() {
        var investment = setUpInvestment(context());
        var firstSale = sellShareFrom(investment);
        var secondSale = sellShareFrom(investment);
        context().receivesCommands(firstSale, secondSale);
        var expected = investmentViewAfter(firstSale, secondSale, investment);

        context().assertState(investment.getId(), expected);
    }

    @Test
    @DisplayName("increase the amount of money in the `WalletBalance` projection")
    void increaseWalletBalance() {
        var wallet = setUpReplenishedWallet(context());
        var purchaseCommand = purchaseShares(wallet);
        context().receivesCommand(purchaseCommand);

        var sellCommand = sellShareAfter(purchaseCommand);
        context().receivesCommand(sellCommand);
        var expected = walletBalanceAfter(purchaseCommand, sellCommand, wallet);

        context().assertState(wallet.getId(), expected);
    }

    @Test
    @DisplayName("have an expected state")
    void state() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        context().receivesCommand(command);
        var expected = sharesSaleInitiatedBy(command);

        context().assertState(command.getSaleProcess(), expected);
    }

    @Test
    @DisplayName("issue the `ReserveShares` command")
    void reserveShares() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        context().receivesCommand(command);
        var expected = reserveSharesWith(command);

        context().assertCommands()
                 .withType(ReserveShares.class)
                 .message(0)
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("issue the `SellSharesOnMarket` command")
    void sellSharesOnMarket() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        context().receivesCommand(command);
        var expected = sellSharesOnMarketWith(command);

        context().assertCommands()
                 .withType(SellSharesOnMarket.class)
                 .message(0)
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("issue the `RechargeBalance` command")
    void rechargeBalance() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        context().receivesCommand(command);
        var expected = rechargeBalanceWith(command);

        context().assertCommands()
                 .withType(RechargeBalance.class)
                 .message(1)
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("issue the `CompleteSharesReservation` command")
    void completeSharesReservation() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        context().receivesCommand(command);
        var expected = completeSharesReservationWith(command);

        context().assertCommands()
                 .withType(CompleteSharesReservation.class)
                 .message(0)
                 .isEqualTo(expected);
    }

    @Test
    @DisplayName("emit the `SharesSold` event")
    void sharesSold() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        context().receivesCommand(command);
        var expected = sharesSoldBy(command, investment);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("emit the `SharesSaleFailed` " +
            "due to insufficient quantity of shares owned")
    void sharesSaleFailedAfterInsufficientShares() {
        var investment = setUpInvestment(context());
        var command = sellMoreSharesThanIn(investment);
        context().receivesCommand(command);
        var expected = sharesSaleFailedAfter(command);

        context().assertEvent(expected);
    }

    @Test
    @DisplayName("issue the `CancelSharesReservation` command " +
            "after error in the shares market")
    void cancelSharesReservation() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        RejectingMarket.switchToRejectionMode();
        context().receivesCommand(command);
        var expected = cancelSharesReservationBy(command);

        context().assertCommands()
                 .withType(CancelSharesReservation.class)
                 .message(0)
                 .isEqualTo(expected);
        RejectingMarket.switchToEventsMode();
    }

    @Test
    @DisplayName("emit the `SharesSaleFailed` event " +
            "after error in the shares market")
    void sharesSaleFailedAfterError() {
        var investment = setUpInvestment(context());
        var command = sellShareFrom(investment);
        RejectingMarket.switchToRejectionMode();
        context().receivesCommand(command);
        var expected = sharesSaleFailedAfter(command);

        context().assertEvent(expected);
        context().assertState(investment.getId(), investment);
        RejectingMarket.switchToEventsMode();
    }
}
