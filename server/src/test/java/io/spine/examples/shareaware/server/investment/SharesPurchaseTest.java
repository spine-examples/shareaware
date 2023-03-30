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
import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.Investment;
import io.spine.examples.shareaware.investment.SharesPurchase;
import io.spine.examples.shareaware.investment.command.AddShares;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.event.SharesPurchaseFailed;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.market.command.ObtainShares;
import io.spine.examples.shareaware.server.given.PurchaseTestContext;
import io.spine.examples.shareaware.server.given.RejectingMarket;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import io.spine.testing.server.model.ModelTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.InvestmentTestEnv.*;
import static io.spine.examples.shareaware.server.given.WalletTestEnv.setUpReplenishedWallet;
import static io.spine.examples.shareaware.server.given.WalletTestEnv.setUpWallet;

@DisplayName("`SharesPurchase` should")
public final class SharesPurchaseTest extends ContextAwareTest {

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
        return PurchaseTestContext.newBuilder();
    }

    @Nested
    @DisplayName("reduce the wallet balance")
    class WithdrawMoneyFromBalance {

        @Test
        @DisplayName("for the purchase price amount")
        void event() {
            Wallet wallet = setUpReplenishedWallet(context());
            UserId user = wallet.getId()
                                .getOwner();
            PurchaseShares firstPurchase = purchaseSharesFor(user);
            PurchaseShares secondPurchase = purchaseSharesFor(user);
            Wallet walletAfterPurchase =
                    walletAfter(firstPurchase, secondPurchase, wallet);
            context().receivesCommands(firstPurchase, secondPurchase);

            context().assertState(wallet.getId(), walletAfterPurchase);
        }

        @Test
        @DisplayName("emitting the `MoneyReserved` event")
        void moneyReserved() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            MoneyReserved expected = moneyReservedBy(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `ReservedMoneyDebited` event")
        void debitMoney() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            ReservedMoneyDebited expected = reservedMoneyDebitedBy(command, wallet);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `InsufficientFunds` rejection")
        void insufficientFunds() {
            WalletId wallet = setUpWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getOwner());
            InsufficientFunds expected = insufficientFundsIn(wallet, command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `MoneyReservationCanceled` event")
        void reservationCanceled() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            MoneyReservationCanceled expected = moneyReservationCanceledAfter(command);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }
    }

    @Nested
    @DisplayName("change the `Investment` state")
    class InvestmentState {

        @Test
        @DisplayName("by adding shares to it")
        void addShares() {
            Wallet wallet = setUpReplenishedWallet(context());
            UserId user = wallet.getId()
                                .getOwner();
            ShareId share = ShareId.generate();
            PurchaseShares firstPurchase = purchaseSharesFor(user, share);
            PurchaseShares secondPurchase = purchaseSharesFor(user, share);
            Investment expected = investmentAfter(firstPurchase, secondPurchase);
            InvestmentId investmentId = InvestmentId
                    .newBuilder()
                    .setShare(share)
                    .setOwner(user)
                    .vBuild();
            context().receivesCommands(firstPurchase, secondPurchase);

            context().assertState(investmentId, expected);
        }
    }

    @Nested
    @DisplayName("be leb by shares purchase process")
    class PurchaseProcess {

        @Test
        @DisplayName("with state")
        void state() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            SharesPurchase expected = sharesPurchaseStateWhen(command);
            context().receivesCommand(command);

            context().assertState(command.getPurchaseProcess(), expected);
        }

        @Test
        @DisplayName("which issues the `ReserveMoney` command")
        void reserveMoney() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            ReserveMoney expected = reserveMoneyInitiatedBy(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(ReserveMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `ObtainShares` command")
        void obtainShares() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            ObtainShares expected = obtainSharesWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(ObtainShares.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `AddShares` command")
        void addShares() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            AddShares expected = addSharesWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(AddShares.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which issues the `DebitReservedMoney` command")
        void debitReservedMoney() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            DebitReservedMoney expected = debitReservedMoneyWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(DebitReservedMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `SharesPurchased` event")
        void sharesPurchased() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            SharesPurchased expected = sharesPurchasedAsResultOf(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("which emits the `SharesPurchasedFailed` event when insufficient funds in the wallet")
        void processFailedAfterInsufficientFunds() {
            WalletId wallet = setUpWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getOwner());
            SharesPurchaseFailed expected = sharesPurchaseFailedAsResultOf(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("which issues the `CancelMoneyReservation` command")
        void cancelMoneyReservation() {
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            CancelMoneyReservation expected = cancelMoneyReservationAfter(command);
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
            Wallet wallet = setUpReplenishedWallet(context());
            PurchaseShares command = purchaseSharesFor(wallet.getId()
                                                             .getOwner());
            SharesPurchaseFailed expected = sharesPurchaseFailedAsResultOf(command);
            RejectingMarket.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            RejectingMarket.switchToEventsMode();
        }
    }
}
