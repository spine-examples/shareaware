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

import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.server.FreshContextTest;
import io.spine.examples.shareaware.server.given.RejectingPaymentProcess;
import io.spine.examples.shareaware.server.given.WalletTestContext;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.server.BoundedContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.*;
import static io.spine.examples.shareaware.server.wallet.WalletReplenishmentProcess.*;

@DisplayName("`WalletWithdrawal` should")
public final class WalletWithdrawalTest extends FreshContextTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return WalletTestContext.newBuilder();
    }

    @Nested
    @DisplayName("reduce the wallet balance")
    class WithdrawMoneyFromBalance {

        @Test
        @DisplayName("for expected amount of money")
        void entity() {
            var wallet = setUpReplenishedWallet(context());
            var firstWithdraw = withdrawMoneyFrom(wallet.getId());
            var secondWithdraw = withdrawMoneyFrom(wallet.getId());
            var expectedWallet = walletWhichWasWithdrawnBy(firstWithdraw,
                                                           secondWithdraw,
                                                           wallet);
            context().receivesCommands(firstWithdraw, secondWithdraw);

            context().assertState(wallet.getId(), expectedWallet);
        }

        @Test
        @DisplayName("emitting the `ReservedMoneyDebited` event")
        void debitMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = reservedMoneyDebitedFrom(wallet, command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `MoneyReserved` event")
        void reserveMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = moneyReservedBy(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `InsufficientFunds` rejection")
        void insufficientFunds() {
            var wallet = setUpWallet(context());
            var command = withdrawMoneyFrom(wallet);
            var expected = insufficientFundsIn(wallet, command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }

        @Test
        @DisplayName("emitting the `MoneyReservationCanceled` event")
        void cancelMoneyReservation() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var event = moneyReservationCanceledBy(command);
            RejectingPaymentProcess.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertState(wallet.getId(), wallet);
            context().assertEvent(event);
            RejectingPaymentProcess.switchToEventsMode();
        }
    }

    @Nested
    @DisplayName("reduce the value of `WalletBalance` projection")
    class ReduceWalletBalanceProjection {

        @Test
        void balance() {
            var wallet = setUpReplenishedWallet(context());
            var firstWithdraw = withdrawMoneyFrom(wallet.getId());
            var secondWithdraw = withdrawMoneyFrom(wallet.getId());
            var expected = walletBalanceReducedBy(firstWithdraw,
                                                  secondWithdraw,
                                                  wallet);
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
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = walletWithdrawalBy(command);
            context().receivesCommand(command);

            context().assertState(command.getWithdrawalProcess(), expected);
        }

        @Test
        @DisplayName("which sends the `ReserveMoney` command")
        void reserveMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = reserveMoneyWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(ReserveMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which sends the `TransferMoneyToUser` command")
        void transferMoneyToUser() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = transferMoneyToUserWith(command, shareAwareIban);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(TransferMoneyToUser.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which sends the `DebitReservedMoney` command")
        void debitReservedMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = debitReservedMoneyWith(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(DebitReservedMoney.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `WalletWithdrawn` event and archives itself after it")
        void event() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = moneyWithdrawnBy(command, wallet);
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(command.getWithdrawalProcess(),
                                   WalletWithdrawalProcess.class)
                     .archivedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("which emits the `WalletNotWithdrawn` event when insufficient funds on the wallet")
        void insufficientFunds() {
            var wallet = setUpWallet(context());
            var command = withdrawMoneyFrom(wallet);
            var withdrawalProcess = command.getWithdrawalProcess();
            var expected = moneyNotWithdrawnBy(withdrawalProcess);
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(withdrawalProcess, WalletWithdrawalProcess.class)
                     .archivedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("which sends `CancelMoneyReservation` command when something went wrong in payment system")
        void moneyCannotBeTransferredToUser() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = cancelMoneyReservationBy(command);
            RejectingPaymentProcess.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(CancelMoneyReservation.class)
                     .message(0)
                     .isEqualTo(expected);
            RejectingPaymentProcess.switchToEventsMode();
        }

        @Test
        @DisplayName("which emits the `WalletNotWithdrawn` event when money reservation was canceled")
        void moneyReservationCanceled() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var withdrawalProcess = command.getWithdrawalProcess();
            var expected = moneyNotWithdrawnBy(withdrawalProcess);
            RejectingPaymentProcess.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(withdrawalProcess, WalletWithdrawalProcess.class)
                     .archivedFlag()
                     .isTrue();
            RejectingPaymentProcess.switchToEventsMode();
        }
    }

    @Nested
    @DisplayName("interact with `PaymentGateway`")
    class PaymentGateway {

        @Test
        @DisplayName("which emits the `MoneyTransferredToUser` event")
        void transferMoney() {
            var wallet = setUpReplenishedWallet(context());
            var command = withdrawMoneyFrom(wallet.getId());
            var expected = moneyTransferredToUserBy(command);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }
    }
}
