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
import io.spine.client.Client;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.market.AvailableMarketShares;
import io.spine.examples.shareaware.server.e2e.given.SubscriptionOutcome;
import io.spine.examples.shareaware.server.e2e.given.E2ETestUser;
import io.spine.examples.shareaware.server.e2e.given.WithClient;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.*;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * End-to-end test that describes such a scenario:
 * <pre>
 *     - the user attempts to purchase shares with no money in the wallet,
 *     - the user replenishes his wallet for 500 dollars,
 *     - the user successfully purchases one 'Tesla' share,
 *     - the user withdraws all his money from the wallet.
 * </pre>
 */
final class SharePurchaseTest extends WithClient {

    @Test
    @DisplayName("Should purchase one tesla share and withdraw all the money after this")
    void test() throws ExecutionException, InterruptedException {
        SharePurchaseUser user = new SharePurchaseUser(client());

        WalletBalance initialBalance = user.looksAtWalletBalance();
        sleepUninterruptibly(ofMillis(1500));
        List<Share> shares = user.looksAtShares();
        Share tesla = chooseTeslaShareFrom(shares);

        WalletBalance balanceAfterFailedPurchase = user.attemptsToPurchaseShare(tesla);
        assertThat(balanceAfterFailedPurchase).isEqualTo(initialBalance);

        WalletBalance balanceAfterReplenishment = user.replenishesWalletFor(usd(500));
        WalletBalance expectedBalanceAfterReplenishment =
                walletBalanceWith(usd(500), user.walletId());
        assertThat(balanceAfterReplenishment).isEqualTo(expectedBalanceAfterReplenishment);

        InvestmentView investmentInTesla = user.purchasesShare(tesla);
        InvestmentView expectedInvestmentInTesla = investmentAfterTeslaPurchase(tesla, user.id());
        WalletBalance balanceAfterPurchase = user.looksAtWalletBalance();
        WalletBalance expectedBalanceAfterPurchase =
                balanceAfterTeslaPurchase(tesla.getPrice(), balanceAfterReplenishment);
        assertThat(investmentInTesla).isEqualTo(expectedInvestmentInTesla);
        assertThat(balanceAfterPurchase).isEqualTo(expectedBalanceAfterPurchase);

        WalletBalance walletAfterWithdrawal = user.withdrawsAllHisMoney();
        assertThat(walletAfterWithdrawal).isEqualTo(zeroWalletBalance(user.walletId()));
    }

    /**
     * The user for a {@link SharePurchaseTest} that can perform actions
     * that describe the test scenario.
     */
    private class SharePurchaseUser extends E2ETestUser {

        private SharePurchaseUser(Client client) {
            super(client);
        }

        /**
         * Describes the user attempting to purchase a share with no money in the wallet.
         *
         * <p>As a result the user should get the {@link InsufficientFunds} rejection.
         */
        private WalletBalance attemptsToPurchaseShare(Share tesla)
                throws ExecutionException, InterruptedException {
            PurchaseShares purchaseTeslaShare = purchaseShareFor(id(), tesla);
            InsufficientFunds expectedInsufficientFunds =
                    insufficientFundsAfter(purchaseTeslaShare);

            CompletableFuture<InsufficientFunds> actualInsufficientFunds =
                    subscribeToEventAndForget(InsufficientFunds.class);
            command(purchaseTeslaShare);

            assertThat(actualInsufficientFunds.get()).isEqualTo(expectedInsufficientFunds);
            return looksAtWalletBalance();
        }

        /**
         * Describes the user's action to replenish his wallet.
         *
         * <p>As a result, the wallet should be replenished on the passed amount.
         */
        private WalletBalance replenishesWalletFor(Money amount)
                throws ExecutionException, InterruptedException {
            ReplenishWallet replenishWallet = replenishWallet(walletId(), amount);
            WalletReplenished expectedWalletReplenished =
                    walletReplenishedAfter(replenishWallet);

            CompletableFuture<WalletReplenished> actualWalletReplenished =
                    subscribeToEventAndForget(WalletReplenished.class);
            SubscriptionOutcome<WalletBalance> actualBalance =
                    subscribeToState(WalletBalance.class);
            command(replenishWallet);

            assertThat(actualWalletReplenished.get())
                    .isEqualTo(expectedWalletReplenished);
            cancel(actualBalance.subscription());
            return actualBalance.future()
                                .get();
        }

        /**
         * Describes the user's action to purchase a share with the replenished wallet.
         *
         * <p>As a result, the share should be purchased and added to the user's investment.
         */
        private InvestmentView purchasesShare(Share tesla)
                throws ExecutionException, InterruptedException {
            PurchaseShares purchaseShares = purchaseShareFor(id(), tesla);
            SharesPurchased expectedSharesPurchased = sharesPurchasedAfter(purchaseShares);

            CompletableFuture<SharesPurchased> actualSharesPurchased =
                    subscribeToEventAndForget(SharesPurchased.class);
            CompletableFuture<InvestmentView> actualInvestment =
                    subscribeToStateAndForget(InvestmentView.class);
            command(purchaseShares);

            assertThat(actualSharesPurchased.get()).isEqualTo(expectedSharesPurchased);
            return actualInvestment.get();
        }

        /**
         * Describes the user's action to withdraw all money from the wallet.
         *
         * <p>As a result, the wallet balance should be zero.
         */
        private WalletBalance withdrawsAllHisMoney()
                throws ExecutionException, InterruptedException {
            WalletBalance currentBalance = looksAtWalletBalance();
            WithdrawMoney withdrawAllMoney = withdrawAllMoney(currentBalance, walletId());
            MoneyWithdrawn expectedMoneyWithdrawn = moneyWithdrawnAfter(withdrawAllMoney,
                                                                        currentBalance);

            CompletableFuture<MoneyWithdrawn> actualWithdrawnMoney =
                    subscribeToEventAndForget(MoneyWithdrawn.class);
            SubscriptionOutcome<WalletBalance> balanceAfterWithdrawal =
                    subscribeToState(WalletBalance.class);
            command(withdrawAllMoney);

            assertThat(actualWithdrawnMoney.get()).isEqualTo(expectedMoneyWithdrawn);
            return balanceAfterWithdrawal.future()
                                         .get();
        }

        /**
         * Describes the user's action to look at his wallet balance.
         */
        private WalletBalance looksAtWalletBalance() {
            ImmutableList<WalletBalance> balances = lookAt(WalletBalance.class);
            if (balances.size() != 1) {
                fail();
            }
            return balances.get(0);
        }

        /**
         * Describes the user's action to look at the available shares on the market.
         */
        private List<Share> looksAtShares() throws ExecutionException,
                                                   InterruptedException {
            CompletableFuture<List<Share>> shares = new CompletableFuture<>();
            client().onBehalfOf(id())
                    .subscribeTo(AvailableMarketShares.class)
                    .observe(projection -> shares.complete(projection.getShareList()))
                    .post();
            return shares.get();
        }
    }
}
