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

package io.spine.examples.shareaware.server.e2e;

import com.google.common.collect.ImmutableList;
import io.spine.core.UserId;
import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.server.e2e.given.FutureAndSubscription;
import io.spine.examples.shareaware.server.e2e.given.WithClient;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.OneTimeVisitorTestEnv.*;
import static io.spine.examples.shareaware.server.given.GivenWallet.createWallet;
import static io.spine.examples.shareaware.server.given.GivenWallet.walletId;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.fail;

class OneTimeVisitorTest extends WithClient {

    @Test
    void oneTimeVisit() throws ExecutionException, InterruptedException {
        UserId user = GivenUserId.generated();
        WalletId walletId = walletId(user);
        sleepUninterruptibly(ofMillis(1500));
        List<Share> shares = shares(user).get();
        Share tesla = tesla(shares);

        WalletBalance balanceAfterCreation = createWalletFor(user);
        WalletBalance zeroBalance = zeroWalletBalance(walletId);
        assertThat(balanceAfterCreation).isEqualTo(zeroBalance);

        WalletBalance balanceAfterFailedPurchase = tryToPurchaseTeslaShareFor(user, tesla);
        assertThat(balanceAfterFailedPurchase).isEqualTo(balanceAfterCreation);

        Money replenishmentAmount = usd(500);
        WalletBalance balanceAfterReplenishment = replenishWalletFor(replenishmentAmount, walletId);
        WalletBalance expectedBalanceAfterReplenishment =
                walletBalanceWith(replenishmentAmount, walletId);
        assertThat(balanceAfterReplenishment).isEqualTo(expectedBalanceAfterReplenishment);

        InvestmentView investmentInTesla = purchaseTeslaShareFor(user, tesla);
        InvestmentView expectedInvestmentInTesla = investmentAfterTeslaPurchase(tesla, user);
        WalletBalance balanceAfterPurchase = lookAtWalletBalanceOf(user);
        WalletBalance expectedBalanceAfterPurchase =
                balanceAfterTeslaPurchase(tesla.getPrice(), balanceAfterReplenishment);
        assertThat(investmentInTesla).isEqualTo(expectedInvestmentInTesla);
        assertThat(balanceAfterPurchase).isEqualTo(expectedBalanceAfterPurchase);

        WalletBalance walletAfterWithdrawal = withdrawAllMoneyFrom(walletId);
        assertThat(walletAfterWithdrawal).isEqualTo(zeroBalance);
    }

    private WalletBalance createWalletFor(UserId user)
            throws ExecutionException, InterruptedException {
        WalletId walletId = walletId(user);
        CreateWallet createWallet = createWallet(walletId);

        CompletableFuture<WalletCreated> actualWalletCreated =
                subscribeToEventAndForget(WalletCreated.class, user);
        FutureAndSubscription<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class, user);
        command(createWallet, user);

        assertThat(actualWalletCreated.get()).isEqualTo(walletCreatedWith(walletId));
        cancel(actualBalance.subscription());
        return actualBalance.future()
                            .get();
    }

    private WalletBalance tryToPurchaseTeslaShareFor(UserId user, Share tesla)
            throws ExecutionException, InterruptedException {
        PurchaseShares purchaseTeslaShare = purchaseShareFor(user, tesla);
        InsufficientFunds expectedInsufficientFunds = insufficientFundsAfter(purchaseTeslaShare);

        CompletableFuture<InsufficientFunds> actualInsufficientFunds =
                subscribeToEventAndForget(InsufficientFunds.class, user);
        command(purchaseTeslaShare, user);

        assertThat(actualInsufficientFunds.get()).isEqualTo(expectedInsufficientFunds);
        return lookAtWalletBalanceOf(user);
    }

    private WalletBalance replenishWalletFor(Money amount, WalletId walletId)
            throws ExecutionException, InterruptedException {
        ReplenishWallet replenishWallet = replenishWallet(walletId, amount);
        WalletReplenished expectedWalletReplenished = walletReplenishedAfter(replenishWallet);

        UserId user = walletId.getOwner();
        CompletableFuture<WalletReplenished> actualWalletReplenished =
                subscribeToEventAndForget(WalletReplenished.class, user);
        FutureAndSubscription<WalletBalance> actualBalance =
                subscribeToState(WalletBalance.class, user);
        command(replenishWallet, user);

        assertThat(actualWalletReplenished.get())
                .isEqualTo(expectedWalletReplenished);
        cancel(actualBalance.subscription());
        return actualBalance.future()
                            .get();
    }

    private InvestmentView purchaseTeslaShareFor(UserId user, Share tesla)
            throws ExecutionException, InterruptedException {
        PurchaseShares purchaseShares = purchaseShareFor(user, tesla);
        SharesPurchased expectedSharesPurchased = sharesPurchasedAfter(purchaseShares);

        CompletableFuture<SharesPurchased> actualSharesPurchased =
                subscribeToEventAndForget(SharesPurchased.class, user);
        CompletableFuture<InvestmentView> actualInvestment =
                subscribeToStateAndForget(InvestmentView.class, user);
        command(purchaseShares, user);

        assertThat(actualSharesPurchased.get()).isEqualTo(expectedSharesPurchased);
        return actualInvestment.get();
    }

    private WalletBalance withdrawAllMoneyFrom(WalletId wallet)
            throws ExecutionException, InterruptedException {
        UserId user = wallet.getOwner();
        WalletBalance currentBalance = lookAtWalletBalanceOf(user);
        WithdrawMoney withdrawAllMoney = withdrawAllMoney(currentBalance, wallet);
        MoneyWithdrawn expectedMoneyWithdrawn = moneyWithdrawnAfter(withdrawAllMoney,
                                                                    currentBalance);

        CompletableFuture<MoneyWithdrawn> actualWithdrawnMoney =
                subscribeToEventAndForget(MoneyWithdrawn.class, user);
        FutureAndSubscription<WalletBalance> balanceAfterWithdrawal =
                subscribeToState(WalletBalance.class, user);
        command(withdrawAllMoney, user);

        assertThat(actualWithdrawnMoney.get()).isEqualTo(expectedMoneyWithdrawn);
        cancel(balanceAfterWithdrawal.subscription());
        return balanceAfterWithdrawal.future()
                                     .get();
    }

    private WalletBalance lookAtWalletBalanceOf(UserId user) {
        ImmutableList<WalletBalance> balances = lookAt(WalletBalance.class, user);
        if (balances.size() != 1) {
            fail();
        }
        return balances.get(0);
    }

    private CompletableFuture<List<Share>> shares(UserId user) {
        CompletableFuture<List<Share>> shares = new CompletableFuture<>();
        client().onBehalfOf(user)
                .subscribeTo(AvailableMarketShares.class)
                .observe(projection -> shares.complete(projection.getShareList()))
                .post();
        return shares;
    }
}
