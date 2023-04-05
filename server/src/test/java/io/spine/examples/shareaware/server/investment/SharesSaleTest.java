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

import io.spine.core.UserId;
import io.spine.examples.shareaware.investment.Investment;
import io.spine.examples.shareaware.investment.SharesSale;
import io.spine.examples.shareaware.investment.command.CancelSharesReservation;
import io.spine.examples.shareaware.investment.command.CompleteSharesReservation;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.command.ReserveShares;
import io.spine.examples.shareaware.investment.command.SellShares;
import io.spine.examples.shareaware.investment.event.SharesReservationCanceled;
import io.spine.examples.shareaware.investment.event.SharesReservationCompleted;
import io.spine.examples.shareaware.investment.event.SharesReserved;
import io.spine.examples.shareaware.investment.event.SharesSaleFailed;
import io.spine.examples.shareaware.investment.event.SharesSold;
import io.spine.examples.shareaware.investment.rejection.Rejections.InsufficientShares;
import io.spine.examples.shareaware.market.command.SellSharesOnMarket;
import io.spine.examples.shareaware.server.investment.given.InvestmentTestContext;
import io.spine.examples.shareaware.server.investment.given.RejectingMarket;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import io.spine.testing.server.model.ModelTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.*;
import static io.spine.examples.shareaware.server.investment.given.InvestmentTestEnv.*;
import static io.spine.examples.shareaware.server.investment.given.SharesSaleTestEnv.*;

@DisplayName("`SharesSale` should")
public class SharesSaleTest extends ContextAwareTest {

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
        return InvestmentTestContext.newBuilder();
    }

    @Nested
    @DisplayName("recharge the wallet balance")
    class RechargeWallet {

        @Test
        @DisplayName("for the sell price")
        void state() {
            Wallet wallet = setUpReplenishedWallet(context());
            UserId user = wallet.getId()
                                .getOwner();
            PurchaseShares purchaseCommand = purchaseSharesFor(user);
            context().receivesCommand(purchaseCommand);

            SellShares sellCommand = sellShareAfter(purchaseCommand);
            context().receivesCommand(sellCommand);
            Wallet expected = walletAfter(purchaseCommand, sellCommand, wallet);

            context().assertState(wallet.getId(), expected);
        }

        @Test
        @DisplayName("emitting the `BalanceRecharged` event")
        void event() {
            Wallet wallet = setUpReplenishedWallet(context());
            UserId user = wallet.getId()
                                .getOwner();
            PurchaseShares purchaseCommand = purchaseSharesFor(user);
            context().receivesCommand(purchaseCommand);

            SellShares sellCommand = sellShareAfter(purchaseCommand);
            context().receivesCommand(sellCommand);
            BalanceRecharged expected = balanceRechargedBy(sellCommand);

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
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            Investment expected = investmentAfter(command, investment);

            context().assertState(investment.getId(), expected);
        }

        @Test
        @DisplayName("by emitting the `SharesReserved` event")
        void sharesReserved() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            SharesReserved expected = sharesReservedBy(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("by emitting the `SharesReservationCompleted` event")
        void sharesReservationCompleted() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            SharesReservationCompleted expected =
                    sharesReservationCompletedBy(command, investment);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("by emitting the `SharesReservationCanceled event " +
                "after error in the shares market")
        void sharesReservationCanceled() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);
            SharesReservationCanceled expected =
                    sharesReservationCanceledBy(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }

        @Test
        @DisplayName("by emitting the `InsufficientShares` rejection")
        void insufficientShares() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellMoreSharesThanIn(investment);
            context().receivesCommand(command);
            InsufficientShares expected = insufficientSharesCausedBy(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("be led by shares sale process")
    class SaleProcess {

        @Test
        @DisplayName("with state")
        void state() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            SharesSale expected = sharesSaleInitiatedBy(command);

            context().assertState(command.getSaleProcess(), expected);
        }

        @Test
        @DisplayName("which issues the `ReserveShares` command")
        void reserveShares() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            ReserveShares expected = reserveSharesWith(command);

            context().assertCommands()
                     .withType(ReserveShares.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `SellSharesOnMarket` command")
        void sellSharesOnMarket() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            SellSharesOnMarket expected = sellSharesOnMarketWith(command);

            context().assertCommands()
                     .withType(SellSharesOnMarket.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `RechargeBalance` command")
        void rechargeBalance() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            RechargeBalance expected = rechargeBalanceWith(command);

            context().assertCommands()
                     .withType(RechargeBalance.class)
                     .message(1)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `CompleteSharesReservation` command")
        void completeSharesReservation() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            CompleteSharesReservation expected =
                    completeSharesReservationWith(command);

            context().assertCommands()
                     .withType(CompleteSharesReservation.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `SharesSold` event")
        void sharesSold() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            context().receivesCommand(command);
            SharesSold expected = sharesSoldBy(command, investment);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("which emits the `SharesSaleFailed` " +
                "due to insufficient shares in the investment")
        void sharesSaleFailedAfterInsufficientShares() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellMoreSharesThanIn(investment);
            context().receivesCommand(command);
            SharesSaleFailed expected = sharesSaleFailedAfter(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("which issues the `CancelSharesReservation` command " +
                "after error in the shares market")
        void cancelSharesReservation() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);
            CancelSharesReservation expected = cancelSharesReservationWith(command);

            context().assertCommands()
                     .withType(CancelSharesReservation.class)
                     .message(0)
                     .isEqualTo(expected);
            RejectingMarket.switchToEventsMode();
        }

        @Test
        @DisplayName("which emits the `SharesSaleFailed` event " +
                "after error in the shares market")
        void sharesSaleFailedAfterError() {
            Investment investment = setUpInvestment(context());
            SellShares command = sellShareFrom(investment);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);
            SharesSaleFailed expected = sharesSaleFailedAfter(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }
    }
}
