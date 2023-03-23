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

package io.spine.examples.shareaware.server.wallet;

import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.server.given.WithdrawalTestContext;
import io.spine.examples.shareaware.server.given.RejectControllablePaymentSystem;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletWithdrawal;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyNotWithdrawn;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import io.spine.testing.server.model.ModelTests;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.*;
import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.*;

@DisplayName("`WalletWithdrawal` should")
public final class WalletWithdrawalTest extends ContextAwareTest {

    @BeforeAll
    static void beforeAll() {
        ModelTests.dropAllModels();
    }

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return WithdrawalTestContext.newBuilder();
    }

    @Nested
    @DisplayName("reduce the wallet balance")
    class WithdrawMoneyFromBalance {

        @Test
        @DisplayName("for 400 USD")
        void entity() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney firstWithdraw =
                    withdraw(wallet.getId());
            WithdrawMoney secondWithdraw =
                    withdraw(wallet.getId());
            Money withdrawalAmount =
                    sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
            Money expectedBalance =
                    subtract(wallet.getBalance(), withdrawalAmount);
            Wallet expectedWallet = Wallet
                    .newBuilder()
                    .setId(wallet.getId())
                    .setBalance(expectedBalance)
                    .vBuild();
            context().receivesCommands(firstWithdraw, secondWithdraw);

            context().assertState(wallet.getId(), expectedWallet);
        }

        @Test
        @DisplayName("emitting the `ReservedMoneyDebited` event")
        void debitMoney() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            Money reducedBalance = subtract(wallet.getBalance(), command.getAmount());
            ReservedMoneyDebited expected = ReservedMoneyDebited
                    .newBuilder()
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setWallet(wallet.getId())
                    .setCurrentBalance(reducedBalance)
                    .vBuild();
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `MoneyReserved` event")
        void reserveMoney() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            MoneyReserved expected = MoneyReserved
                    .newBuilder()
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setWallet(wallet.getId())
                    .setAmount(command.getAmount())
                    .vBuild();
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `InsufficientFunds` rejection")
        void insufficientFunds() {
            WalletId wallet =
                    setupWallet(context());
            WithdrawMoney command =
                    withdraw(wallet);
            InsufficientFunds expected = InsufficientFunds
                    .newBuilder()
                    .setWallet(wallet)
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setAmount(command.getAmount())
                    .vBuild();
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `MoneyReservationCanceled` event")
        void cancelMoneyReservation() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            MoneyReservationCanceled event = MoneyReservationCanceled
                    .newBuilder()
                    .setWallet(wallet.getId())
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .vBuild();
            RejectControllablePaymentSystem.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertState(wallet.getId(), wallet);
            context().assertEvent(event);
            RejectControllablePaymentSystem.switchToEventsMode();
        }
    }

    @Nested
    @DisplayName("reduce the value of `WalletBalance` projection")
    class ReduceWalletBalanceProjection {

        @Test
        void balance() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney firstWithdraw =
                    withdraw(wallet.getId());
            WithdrawMoney secondWithdraw =
                    withdraw(wallet.getId());
            Money withdrawalAmount =
                    sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
            Money expectedBalance =
                    subtract(wallet.getBalance(), withdrawalAmount);
            WalletBalance expected = WalletBalance
                    .newBuilder()
                    .setId(wallet.getId())
                    .setBalance(expectedBalance)
                    .vBuild();
            context().receivesCommands(firstWithdraw, secondWithdraw);

            context().assertState(wallet.getId(), expected);
        }
    }

    @Nested
    @DisplayName("be led by `WalletWithdrawalProcess`")
    class WithdrawalProcess {

        @Test
        @DisplayName("with state")
        void entity() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            WithdrawalId withdrawalId = command.getWithdrawalProcess();
            WalletWithdrawal expected = WalletWithdrawal
                    .newBuilder()
                    .setId(withdrawalId)
                    .setWallet(wallet.getId())
                    .setRecipient(command.getRecipient())
                    .vBuild();
            context().receivesCommand(command);

            context().assertState(withdrawalId, expected);
        }

        @Test
        @DisplayName("which send the `ReserveMoney` command")
        void reserveMoney() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            ReserveMoney expected = ReserveMoney
                    .newBuilder()
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setWallet(command.getWallet())
                    .setAmount(command.getAmount())
                    .vBuild();
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(ReserveMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which sends the `TransferMoneyToUser` command")
        void transferMoneyToUser() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            TransferMoneyToUser expected = TransferMoneyToUser
                    .newBuilder()
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setGateway(PaymentGatewayProcess.ID)
                    .setSender(WalletReplenishmentProcess.shareAwareIban)
                    .setRecipient(command.getRecipient())
                    .setAmount(command.getAmount())
                    .vBuild();
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(TransferMoneyToUser.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which sends the `DebitReservedMoney` command")
        void debitReservedMoney() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            DebitReservedMoney expected = DebitReservedMoney
                    .newBuilder()
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setWallet(wallet.getId())
                    .vBuild();
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(DebitReservedMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `WalletWithdrawn` event and archives itself after it")
        void event() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            WithdrawalId withdrawal = command.getWithdrawalProcess();
            Money reducedBalance = subtract(wallet.getBalance(), command.getAmount());
            MoneyWithdrawn expected = MoneyWithdrawn
                    .newBuilder()
                    .setWithdrawalProcess(withdrawal)
                    .setWallet(command.getWallet())
                    .setCurrentBalance(reducedBalance)
                    .vBuild();
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(withdrawal, WalletWithdrawalProcess.class)
                     .archivedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("which emits the `WalletNotWithdrawn` event when insufficient funds on the wallet")
        void insufficientFunds() {
            WalletId wallet =
                    setupWallet(context());
            WithdrawMoney command =
                    withdraw(wallet);
            WithdrawalId withdrawal = command.getWithdrawalProcess();
            MoneyNotWithdrawn expected = MoneyNotWithdrawn
                    .newBuilder()
                    .setWithdrawalProcess(withdrawal)
                    .vBuild();
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(withdrawal, WalletWithdrawalProcess.class)
                     .archivedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("which sends `CancelMoneyReservation` command when something went wrong in payment system")
        void moneyCannotBeTransferredToUser() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            CancelMoneyReservation expected = CancelMoneyReservation
                    .newBuilder()
                    .setWallet(command.getWallet())
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .vBuild();
            RejectControllablePaymentSystem.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(CancelMoneyReservation.class)
                     .message(0)
                     .isEqualTo(expected);
            RejectControllablePaymentSystem.switchToEventsMode();
        }

        @Test
        @DisplayName("which emits the `WalletNotWithdrawn` event when money reservation was canceled")
        void moneyReservationCanceled() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            MoneyNotWithdrawn expected = MoneyNotWithdrawn
                    .newBuilder()
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .vBuild();
            RejectControllablePaymentSystem.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(command.getWithdrawalProcess(), WalletWithdrawalProcess.class)
                     .archivedFlag()
                     .isTrue();
            RejectControllablePaymentSystem.switchToEventsMode();
        }
    }

    @Nested
    @DisplayName("interact with `PaymentGateway`")
    class PaymentGateway {

        @Test
        @DisplayName("which emit the `MoneyTransferredToUser` event")
        void transferMoney() {
            Wallet wallet =
                    setupReplenishedWallet(context());
            WithdrawMoney command =
                    withdraw(wallet.getId());
            MoneyTransferredToUser expected = MoneyTransferredToUser
                    .newBuilder()
                    .setGetaway(PaymentGatewayProcess.ID)
                    .setWithdrawalProcess(command.getWithdrawalProcess())
                    .setAmount(command.getAmount())
                    .vBuild();
            context().receivesCommand(command);

            context().assertEvent(expected);
        }
    }

    @AfterAll
    static void afterAll() {
        ModelTests.dropAllModels();
    }
}
