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

import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.paymentgateway.rejection.Rejections.MoneyCannotBeTransferredToUser;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
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
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

/**
 * Coordinates the withdrawal of the user's wallet.
 */
public final class WalletWithdrawalProcess
        extends ProcessManager<WithdrawalId, WalletWithdrawal, WalletWithdrawal.Builder> {

    /**
     * Issues a command to reserve money in the user's wallet.
     */
    @Command
    ReserveMoney on(WithdrawMoney c) {
        initState(c);
        return ReserveMoney
                .newBuilder()
                .setWallet(c.getWallet())
                .setWithdrawalProcess(c.getWithdrawalProcess())
                .setMoneyAmount(c.getAmount())
                .vBuild();
    }

    private void initState(WithdrawMoney c) {
        builder()
                .setId(c.getWithdrawalProcess())
                .setWallet(c.getWallet())
                .setRecipient(c.getRecipient());
    }

    /**
     * Issues a command to transfer money from the ShareAware bank account
     * to the user's bank account when money was reserved in the user's wallet.
     */
    @Command
    TransferMoneyToUser on(MoneyReserved e) {
        return TransferMoneyToUser
                .newBuilder()
                .setGateway(PaymentGatewayProcess.ID)
                .setWithdrawalProcess(e.getWithdrawalProcess())
                .setSender(WalletReplenishmentProcess.shareAwareIban)
                .setRecipient(state().getRecipient())
                .setAmount(e.getAmount())
                .vBuild();
    }

    /**
     * Issues a command to debit previously reserved money
     * when the transaction has been completed successfully.
     */
    @Command
    DebitReservedMoney on(MoneyTransferredToUser e) {
        return DebitReservedMoney
                .newBuilder()
                .setWallet(state().getWallet())
                .setWithdrawalProcess(e.getWithdrawalProcess())
                .vBuild();
    }

    /**
     * Issues a command to cancel previously made money reservation
     * when the transaction has failed.
     */
    @Command
    CancelMoneyReservation on(MoneyCannotBeTransferredToUser e) {
        return CancelMoneyReservation
                .newBuilder()
                .setWithdrawalProcess(e.getWithdrawalProcess())
                .setWallet(state().getWallet())
                .vBuild();
    }

    /**
     * Ends the process successfully when reserved money has been debited.
     */
    @React
    MoneyWithdrawn on(ReservedMoneyDebited e) {
        setArchived(true);
        return MoneyWithdrawn
                .newBuilder()
                .setWithdrawalProcess(e.getWithdrawalProcess())
                .setWallet(e.getWallet())
                .setAmount(e.getAmount())
                .vBuild();
    }

    /**
     * Terminates the process when money reservation has been canceled.
     */
    @React
    MoneyNotWithdrawn on(MoneyReservationCanceled e) {
        setArchived(true);
        return MoneyNotWithdrawn
                .newBuilder()
                .setWithdrawalProcess(e.getWithdrawalProcess())
                .vBuild();
    }

    /**
     * Terminates the process when there are insufficient funds in the user's wallet.
     */
    @React
    MoneyNotWithdrawn on(InsufficientFunds e) {
        setArchived(true);
        return MoneyNotWithdrawn
                .newBuilder()
                .setWithdrawalProcess(e.getWithdrawalProcess())
                .vBuild();
    }
}
