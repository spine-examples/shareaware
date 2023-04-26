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

package io.spine.examples.shareaware.server.e2e.given;

import io.spine.core.UserId;
import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.PurchaseId;
import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.WithdrawalOperationId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;

import java.util.Collection;
import java.util.Optional;

import static io.spine.examples.shareaware.MoneyCalculator.subtract;
import static io.spine.examples.shareaware.given.GivenMoney.zero;
import static io.spine.examples.shareaware.server.given.GivenWallet.IBAN;
import static io.spine.examples.shareaware.server.given.GivenWallet.walletId;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.fail;

public class SharePurchaseTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private SharePurchaseTestEnv() {
    }

    public static WalletBalance zeroWalletBalance(WalletId wallet) {
        return WalletBalance
                .newBuilder()
                .setId(wallet)
                .setBalance(zero())
                .vBuild();
    }

    public static PurchaseShares purchaseShareFor(UserId user, Share share) {
        return PurchaseShares
                .newBuilder()
                .setPurchaser(user)
                .setPurchaseProcess(PurchaseId.generate())
                .setQuantity(1)
                .setShare(share.getId())
                .setPrice(share.getPrice())
                .vBuild();
    }

    public static InsufficientFunds insufficientFundsAfter(PurchaseShares command) {
        return InsufficientFunds
                .newBuilder()
                .setWallet(walletId(command.getPurchaser()))
                .setAmount(command.totalCost())
                .setOperation(operationId(command.getPurchaseProcess()))
                .vBuild();
    }

    public static ReplenishWallet replenishWallet(WalletId id, Money amount) {
        return ReplenishWallet
                .newBuilder()
                .setWallet(id)
                .setReplenishment(ReplenishmentId.generate())
                .setIban(IBAN)
                .setMoneyAmount(amount)
                .vBuild();
    }

    public static WalletReplenished walletReplenishedAfter(ReplenishWallet command) {
        return WalletReplenished
                .newBuilder()
                .setWallet(command.getWallet())
                .setReplenishment(command.getReplenishment())
                .setMoneyAmount(command.getMoneyAmount())
                .vBuild();
    }

    public static WalletBalance walletBalanceWith(Money amount, WalletId walletId) {
        return WalletBalance
                .newBuilder()
                .setId(walletId)
                .setBalance(amount)
                .vBuild();
    }

    public static SharesPurchased sharesPurchasedAfter(PurchaseShares command) {
        return SharesPurchased
                .newBuilder()
                .setPurchaser(command.getPurchaser())
                .setPurchaseProcess(command.getPurchaseProcess())
                .setShare(command.getShare())
                .setSharesAvailable(command.getQuantity())
                .vBuild();
    }

    public static WalletBalance balanceAfterTeslaPurchase(Money sharePrice,
                                                          WalletBalance balance) {
        Money reducedBalance = subtract(balance.getBalance(), sharePrice);
        return balance
                .toBuilder()
                .setBalance(reducedBalance)
                .vBuild();
    }

    public static InvestmentView investmentAfterTeslaPurchase(Share tesla, UserId user) {
        InvestmentId id = investmentId(user, tesla.getId());
        return InvestmentView
                .newBuilder()
                .setId(id)
                .setSharesAvailable(1)
                .vBuild();
    }

    public static WithdrawMoney withdrawAllMoney(WalletBalance balance, WalletId wallet) {
        return WithdrawMoney
                .newBuilder()
                .setWithdrawalProcess(WithdrawalId.generate())
                .setWallet(wallet)
                .setRecipient(IBAN)
                .setAmount(balance.getBalance())
                .vBuild();
    }

    public static MoneyWithdrawn moneyWithdrawnAfter(WithdrawMoney command,
                                                     WalletBalance currentBalance) {
        Money reducedBalance = subtract(currentBalance.getBalance(), command.getAmount());
        return MoneyWithdrawn
                .newBuilder()
                .setWithdrawalProcess(command.getWithdrawalProcess())
                .setWallet(command.getWallet())
                .setCurrentBalance(reducedBalance)
                .vBuild();
    }

    private static WithdrawalOperationId operationId(PurchaseId id) {
        return WithdrawalOperationId
                .newBuilder()
                .setPurchase(id)
                .vBuild();
    }

    private static InvestmentId investmentId(UserId user, ShareId share) {
        return InvestmentId
                .newBuilder()
                .setOwner(user)
                .setShare(share)
                .vBuild();
    }

    public static Share tesla(Collection<Share> shares) {
        Optional<Share> tesla = shares
                .stream()
                .filter((share -> share.getCompanyName()
                                       .equals("Tesla Inc.")))
                .findAny();
        if (tesla.isPresent()) {
            return tesla.get();
        }
        fail();
        throw newIllegalArgumentException("No 'Tesla' share were found in the provided `Collection`.");
    }
}
