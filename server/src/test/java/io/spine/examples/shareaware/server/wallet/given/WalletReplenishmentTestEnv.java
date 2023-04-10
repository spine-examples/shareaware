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

import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.ReplenishmentOperationId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.paymentgateway.rejection.Rejections;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.server.wallet.MoneyCalculator;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletReplenishment;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.event.WalletNotReplenished;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.money.Currency;
import io.spine.money.Money;

import static io.spine.examples.shareaware.server.given.GivenMoney.moneyOf;
import static io.spine.examples.shareaware.server.given.GivenWallet.replenishWith;

public class WalletReplenishmentTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private WalletReplenishmentTestEnv() {
    }

    public static Wallet walletReplenishedBy(ReplenishWallet firstReplenishment,
                                             ReplenishWallet secondReplenishment,
                                             WalletId wallet) {
        Money expectedBalance =
                MoneyCalculator.sum(firstReplenishment.getMoneyAmount(),
                                    secondReplenishment.getMoneyAmount());
        return Wallet
                .newBuilder()
                .setId(wallet)
                .setBalance(expectedBalance)
                .vBuild();
    }

    public static WalletReplenishment walletReplenishmentBy(ReplenishWallet command) {
        return WalletReplenishment
                .newBuilder()
                .setWallet(command.getWallet())
                .setId(command.getReplenishment())
                .vBuild();
    }

    public static BalanceRecharged balanceRechargedBy(ReplenishWallet command, WalletId wallet) {
        return BalanceRecharged
                .newBuilder()
                .setWallet(wallet)
                .setCurrentBalance(command.getMoneyAmount())
                .setOperation(operationId(command))
                .vBuild();
    }

    public static WalletReplenished walletReplenishedAfter(ReplenishWallet command) {
        return WalletReplenished
                .newBuilder()
                .setReplenishment(command.getReplenishment())
                .setWallet(command.getWallet())
                .setMoneyAmount(command.getMoneyAmount())
                .vBuild();
    }

    public static Rejections.MoneyCannotBeTransferredFromUser
    moneyCannotBeTransferredFromUserBy(ReplenishmentId replenishmentProcess) {
        return Rejections.MoneyCannotBeTransferredFromUser
                .newBuilder()
                .setReplenishment(replenishmentProcess)
                .vBuild();
    }

    public static WalletNotReplenished
    walletNotReplenishedBy(ReplenishmentId replenishmentProcess) {
        return WalletNotReplenished
                .newBuilder()
                .setReplenishment(replenishmentProcess)
                .vBuild();
    }

    public static TransferMoneyFromUser transferMoneyFromUserBy(ReplenishWallet command,
                                                                Iban recipient) {
        return TransferMoneyFromUser
                .newBuilder()
                .setGateway(PaymentGatewayProcess.ID)
                .setReplenishmentProcess(command.getReplenishment())
                .setAmount(command.getMoneyAmount())
                .setSender(command.getIban())
                .setRecipient(recipient)
                .vBuild();
    }

    public static RechargeBalance rechargeBalanceWhen(ReplenishWallet command) {
        return RechargeBalance
                .newBuilder()
                .setWallet(command.getWallet())
                .setOperation(operationId(command))
                .setMoneyAmount(command.getMoneyAmount())
                .vBuild();
    }

    private static ReplenishmentOperationId operationId(ReplenishWallet c) {
        return ReplenishmentOperationId
                .newBuilder()
                .setReplenishment(c.getReplenishment())
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
}
