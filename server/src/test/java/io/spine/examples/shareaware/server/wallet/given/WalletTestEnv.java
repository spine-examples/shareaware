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

package io.spine.examples.shareaware.server.wallet.given;

import io.spine.examples.shareaware.WithdrawalOperationId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.server.given.GivenMoney;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletWithdrawal;
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyNotWithdrawn;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;

import static io.spine.examples.shareaware.server.given.GivenMoney.moneyOf;
import static io.spine.examples.shareaware.server.given.GivenWallet.*;
import static io.spine.examples.shareaware.server.wallet.MoneyCalculator.*;
import static io.spine.examples.shareaware.server.wallet.given.WalletReplenishmentTestEnv.walletReplenishedBy;

public final class WalletTestEnv {

    /**
     * Prevents instantiation of this test environment.
     */
    private WalletTestEnv() {
    }

    public static WalletId givenId() {
        return WalletId
                .newBuilder()
                .setOwner(GivenUserId.generated())
                .vBuild();
    }

    public static CreateWallet createWallet(WalletId id) {
        return CreateWallet
                .newBuilder()
                .setWallet(id)
                .vBuild();
    }

    /**
     * Generates {@code ReplenishWallet} command on 500 USD for the wallet.
     */
    public static ReplenishWallet replenish(WalletId wallet) {
        Money replenishmentAmount = moneyOf(500, Currency.USD);
        return replenishWith(replenishmentAmount, wallet);
    }

    public static WalletBalance walletBalanceAfterReplenishment(ReplenishWallet firstReplenishment,
                                                                ReplenishWallet secondReplenishment,
                                                                WalletId wallet) {
        Wallet replenishedWallet = walletReplenishedBy(firstReplenishment,
                                                 secondReplenishment,
                                                 wallet);
        return WalletBalance
                .newBuilder()
                .setId(replenishedWallet.getId())
                .setBalance(replenishedWallet.getBalance())
                .vBuild();
    }

    public static WithdrawMoney withdrawMoneyFrom(WalletId wallet) {
        return WithdrawMoney
                .newBuilder()
                .setWithdrawalProcess(WithdrawalId.generate())
                .setWallet(wallet)
                .setRecipient(USERS_IBAN)
                .setAmount(moneyOf(200, Currency.USD))
                .vBuild();
    }

    private static WithdrawalOperationId operationId(WithdrawalId withdrawal) {
        return WithdrawalOperationId
                .newBuilder()
                .setWithdrawal(withdrawal)
                .vBuild();
    }

    public static ReservedMoneyDebited reservedMoneyDebitedFrom(Wallet wallet,
                                                                WithdrawMoney command) {
        Money reducedBalance = subtract(wallet.getBalance(), command.getAmount());
        return ReservedMoneyDebited
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(wallet.getId())
                .setCurrentBalance(reducedBalance)
                .vBuild();
    }

    public static MoneyReserved moneyReservedBy(WithdrawMoney command) {
        return MoneyReserved
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(command.getWallet())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static InsufficientFunds insufficientFundsIn(WalletId wallet, WithdrawMoney command) {
        return InsufficientFunds
                .newBuilder()
                .setWallet(wallet)
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static MoneyReservationCanceled moneyReservationCanceledBy(WithdrawMoney command) {
        return MoneyReservationCanceled
                .newBuilder()
                .setWallet(command.getWallet())
                .setOperation(operationId(command.getWithdrawalProcess()))
                .vBuild();
    }

    public static WalletWithdrawal walletWithdrawalBy(WithdrawMoney command) {
        return WalletWithdrawal
                .newBuilder()
                .setId(command.getWithdrawalProcess())
                .setWallet(command.getWallet())
                .setRecipient(command.getRecipient())
                .vBuild();
    }

    public static ReserveMoney reserveMoneyWith(WithdrawMoney command) {
        return ReserveMoney
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(command.getWallet())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static TransferMoneyToUser transferMoneyToUserWith(WithdrawMoney command,
                                                              Iban sender) {
        return TransferMoneyToUser
                .newBuilder()
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setGateway(PaymentGatewayProcess.ID)
                .setSender(sender)
                .setRecipient(command.getRecipient())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static DebitReservedMoney debitReservedMoneyWith(WithdrawMoney command) {
        return DebitReservedMoney
                .newBuilder()
                .setOperation(operationId(command.getWithdrawalProcess()))
                .setWallet(command.getWallet())
                .vBuild();
    }

    public static MoneyWithdrawn moneyWithdrawnBy(WithdrawMoney command, Wallet wallet) {
        Money reducedBalance = subtract(wallet.getBalance(), command.getAmount());
        return MoneyWithdrawn
                .newBuilder()
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setWallet(command.getWallet())
                .setCurrentBalance(reducedBalance)
                .vBuild();
    }

    public static MoneyNotWithdrawn moneyNotWithdrawnBy(WithdrawalId withdrawalProcess) {
        return MoneyNotWithdrawn
                .newBuilder()
                .setWithdrawalProcess(withdrawalProcess)
                .vBuild();
    }

    public static CancelMoneyReservation cancelMoneyReservationBy(WithdrawMoney command) {
        return CancelMoneyReservation
                .newBuilder()
                .setWallet(command.getWallet())
                .setOperation(operationId(command.getWithdrawalProcess()))
                .vBuild();
    }

    public static MoneyTransferredToUser moneyTransferredToUserBy(WithdrawMoney command) {
        return MoneyTransferredToUser
                .newBuilder()
                .setGetaway(PaymentGatewayProcess.ID)
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setAmount(command.getAmount())
                .vBuild();
    }

    public static Wallet walletWhichWasWithdrawnBy(WithdrawMoney firstWithdraw,
                                                   WithdrawMoney secondWithdraw,
                                                   Wallet wallet) {
        Money withdrawalAmount =
                sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
        Money expectedBalance =
                subtract(wallet.getBalance(), withdrawalAmount);
        return Wallet
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static WalletBalance walletBalanceReducedBy(WithdrawMoney firstWithdraw,
                                                       WithdrawMoney secondWithdraw,
                                                       Wallet wallet) {
        Money withdrawalAmount =
                sum(firstWithdraw.getAmount(), secondWithdraw.getAmount());
        Money expectedBalance =
                subtract(wallet.getBalance(), withdrawalAmount);
        return WalletBalance
                .newBuilder()
                .setId(wallet.getId())
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static Wallet walletWith(Money balance, WalletId id) {
        return Wallet
                .newBuilder()
                .setBalance(balance)
                .setId(id)
                .vBuild();
    }

    public static WalletCreated walletCreatedWith(Money initialBalance, WalletId id) {
        return WalletCreated
                .newBuilder()
                .setWallet(id)
                .setBalance(initialBalance)
                .vBuild();
    }

    public static WalletBalance zeroWalletBalance(WalletId id) {
        return WalletBalance
                .newBuilder()
                .setId(id)
                .setBalance(GivenMoney.zero())
                .vBuild();
    }
}
