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

import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.investment.command.AddShares;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.server.FreshContextTest;
import io.spine.examples.shareaware.server.investment.given.InvestmentTestContext;
import io.spine.examples.shareaware.server.investment.given.RejectingMarket;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.server.BoundedContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.setUpReplenishedWallet;
import static io.spine.examples.shareaware.server.given.WalletTestEnv.setUpWallet;
import static io.spine.examples.shareaware.server.investment.given.InvestmentTestEnv.*;

@DisplayName("`SharesPurchase` should")
public final class SharesPurchaseTest extends FreshContextTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return InvestmentTestContext.newBuilder();
    }

    @Nested
    @DisplayName("reduce the wallet balance")
    class WithdrawMoneyFromBalance {

        @Test
        @DisplayName("for the purchase price amount")
        void walletState() {
            var wallet = setUpReplenishedWallet(context());
            var user = wallet.getId()
                             .getOwner();
            var firstPurchase = purchaseSharesFor(user);
            var secondPurchase = purchaseSharesFor(user);
            var walletAfterPurchase =
                    walletAfter(firstPurchase, secondPurchase, wallet);
            context().receivesCommands(firstPurchase, secondPurchase);

            context().assertState(wallet.getId(), walletAfterPurchase);
        }

        @Test
        @DisplayName("emitting the `MoneyReserved` event")
        void moneyReserved() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = moneyReservedBy(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `ReservedMoneyDebited` event")
        void reservedMoneyDebited() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = reservedMoneyDebitedBy(command, wallet);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `InsufficientFunds` rejection")
        void insufficientFunds() {
            var walletId = setUpWallet(context());
            var command = purchaseSharesFor(walletId.getOwner());
            var expected = insufficientFundsIn(walletId, command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `MoneyReservationCanceled` event")
        void reservationCanceled() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = moneyReservationCanceledAfter(command);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }
    }

    @Nested
    @DisplayName("change the `Investment` state")
    class InvestmentBehavior {

        @Test
        @DisplayName("by adding shares to it")
        void investmentState() {
            var wallet = setUpReplenishedWallet(context());
            var user = wallet.getId()
                             .getOwner();
            var share = ShareId.generate();
            var firstPurchase = purchaseSharesFor(user, share);
            var secondPurchase = purchaseSharesFor(user, share);
            var expected = investmentAfter(firstPurchase, secondPurchase);
            var investmentId = investmentId(user, share);
            context().receivesCommands(firstPurchase, secondPurchase);

            context().assertState(investmentId, expected);
        }

        @Test
        void sharesAdded() {
            var wallet = setUpReplenishedWallet(context());
            var user = wallet.getId()
                             .getOwner();
            var command = purchaseSharesFor(user);
            var expected = sharesAddedBy(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("be led by shares purchase process")
    class PurchaseProcess {

        @Test
        @DisplayName("with state")
        void state() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = sharesPurchaseStateWhen(command);
            context().receivesCommand(command);

            context().assertState(command.getPurchaseProcess(), expected);
        }

        @Test
        @DisplayName("which issues the `ReserveMoney` command")
        void reserveMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = reserveMoneyInitiatedBy(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(ReserveMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `ObtainShares` command")
        void obtainShares() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = obtainSharesWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(ObtainShares.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `AddShares` command")
        void addShares() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = addSharesWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(AddShares.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `DebitReservedMoney` command")
        void debitReservedMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = debitReservedMoneyWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(DebitReservedMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `SharesPurchased` event")
        void sharesPurchased() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = sharesPurchasedAsResultOf(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("which emits the `SharesPurchaseFailed` event when insufficient funds in the wallet")
        void processFailedAfterInsufficientFunds() {
            var walletId = setUpWallet(context());
            var command = purchaseSharesFor(walletId.getOwner());
            var expected = sharesPurchaseFailedAsResultOf(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("which issues the `CancelMoneyReservation` command")
        void cancelMoneyReservation() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = cancelMoneyReservationAfter(command);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(CancelMoneyReservation.class)
                     .message(0)
                     .isEqualTo(expected);
            RejectingMarket.switchToEventsMode();
        }

        @Test
        @DisplayName("which emits the `SharesPurchasedFailed` event after error in the Shares Market")
        void sharesPurchaseFailed() {
            var wallet = setUpReplenishedWallet(context());
            var command = purchaseSharesFor(wallet.getId()
                                                  .getOwner());
            var expected = sharesPurchaseFailedAsResultOf(command);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }
    }

    @Test
    @DisplayName("increase the number of available shares in the `InvestmentView` projection")
    void state() {
        var wallet = setUpReplenishedWallet(context());
        var user = wallet.getId()
                         .getOwner();
        var share = ShareId.generate();
        var firstPurchase = purchaseSharesFor(user, share);
        var secondPurchase = purchaseSharesFor(user, share);
        context().receivesCommands(firstPurchase, secondPurchase);
        var expected = investmentViewAfter(firstPurchase, secondPurchase);

        context().assertState(expected.getId(), expected);
    }

    @Test
    @DisplayName("reduce the balance value in the `WalletBalance` projection")
    void reduceWalletBalance() {
        var wallet = setUpReplenishedWallet(context());
        var user = wallet.getId()
                         .getOwner();
        var share = ShareId.generate();
        var firstPurchase = purchaseSharesFor(user, share);
        var secondPurchase = purchaseSharesFor(user, share);
        context().receivesCommands(firstPurchase, secondPurchase);
        var expected = walletBalanceAfter(firstPurchase, secondPurchase, wallet);

        context().assertState(wallet.getId(), expected);
    }
}
