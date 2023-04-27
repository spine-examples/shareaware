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

import io.spine.client.Client;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.server.e2e.given.E2EUser;
import io.spine.examples.shareaware.server.e2e.given.WithClient;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.E2EUserTestEnv.purchaseSharesFor;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.balanceAfterTeslaPurchase;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.chooseTeslaShareFrom;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.insufficientFundsAfter;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.investmentAfterTeslaPurchase;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.walletBalanceWith;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.zeroWalletBalance;
import static java.time.Duration.ofMillis;

/**
 * End-to-end test that describes such a scenario:
 * <ol>
 *     <li>The user attempts to purchase shares with no money in the wallet.</li>
 *     <li>The user replenishes his wallet for 500 dollars.</li>
 *     <li>The user successfully purchases one 'Tesla' share.</li>
 *     <li>The user withdraws all his money from the wallet.</li>
 * </ol>
 */
final class SharePurchaseTest extends WithClient {

    @Test
    @DisplayName("User should purchase one tesla share and withdraw all the money after this")
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

        InvestmentView investmentInTesla = user.purchasesShares(tesla, 1);
        InvestmentView expectedInvestmentInTesla = investmentAfterTeslaPurchase(tesla, user.id());
        WalletBalance balanceAfterPurchase = user.looksAtWalletBalance();
        WalletBalance expectedBalanceAfterPurchase =
                balanceAfterTeslaPurchase(tesla.getPrice(), balanceAfterReplenishment);
        assertThat(investmentInTesla).isEqualTo(expectedInvestmentInTesla);
        assertThat(balanceAfterPurchase).isEqualTo(expectedBalanceAfterPurchase);

        WalletBalance walletAfterWithdrawal = user.withdrawsAllHisMoney(balanceAfterPurchase);
        assertThat(walletAfterWithdrawal).isEqualTo(zeroWalletBalance(user.walletId()));
    }

    /**
     * The user for a {@link SharePurchaseTest} that can perform actions
     * that describe the test scenario.
     */
    private class SharePurchaseUser extends E2EUser {

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
            PurchaseShares purchaseTeslaShare = purchaseSharesFor(id(), tesla, 1);
            InsufficientFunds expectedInsufficientFunds =
                    insufficientFundsAfter(purchaseTeslaShare);

            CompletableFuture<InsufficientFunds> actualInsufficientFunds =
                    subscribeToEventAndForget(InsufficientFunds.class);
            command(purchaseTeslaShare);

            assertThat(actualInsufficientFunds.get()).isEqualTo(expectedInsufficientFunds);
            return looksAtWalletBalance();
        }

        /**
         * Describes the user's action to withdraw all money from the wallet.
         *
         * <p>As a result, the wallet balance should be zero.
         */
        private WalletBalance withdrawsAllHisMoney(WalletBalance balance) {
            WalletBalance balanceAfterWithdrawal = withdrawMoney(balance.getBalance());
            return balanceAfterWithdrawal;
        }
    }
}
