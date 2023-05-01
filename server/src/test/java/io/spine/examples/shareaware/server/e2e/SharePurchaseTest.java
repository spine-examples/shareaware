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
import io.spine.examples.shareaware.server.e2e.given.E2EUser;
import io.spine.examples.shareaware.server.e2e.given.SubscriptionOutcome;
import io.spine.examples.shareaware.server.e2e.given.WithClient;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds;
import io.spine.server.tuple.EitherOf2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.balanceAfterTeslaPurchase;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.insufficientFundsWith;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.investmentAfterTeslaPurchase;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.pickTesla;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.zeroWalletBalance;

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

        WalletBalance initialBalance = user.signsUp();
        List<Share> shares = user.waitsForSharesToUpdate();
        Share tesla = pickTesla(shares);

        EitherOf2<WalletBalance, InsufficientFunds> failedPurchase = user.purchase(tesla, 1);
        InsufficientFunds expectedInsufficientFunds =
                insufficientFundsWith(user.walletId(), tesla.getPrice());
        assertThat(failedPurchase.getB()).comparingExpectedFieldsOnly()
                                         .isEqualTo(expectedInsufficientFunds);

        WalletBalance balanceAfterReplenishment = user.replenishesWalletFor(usd(500));

        SubscriptionOutcome<InvestmentView> increasedInvestment =
                user.expectsChangesIn(InvestmentView.class);
        EitherOf2<WalletBalance, InsufficientFunds> successfulPurchase = user.purchase(tesla, 1);
        WalletBalance balanceAfterPurchase = successfulPurchase.getA();
        InvestmentView investmentInTesla = user.checksChangesIn(increasedInvestment);
        WalletBalance expectedBalanceAfterPurchase =
                balanceAfterTeslaPurchase(tesla.getPrice(), balanceAfterReplenishment);
        InvestmentView expectedInvestmentInTesla = investmentAfterTeslaPurchase(tesla, user.id());
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
         * Describes the user's action to withdraw all money from the wallet.
         *
         * <p>As a result, the wallet balance should be zero.
         */
        private WalletBalance withdrawsAllHisMoney(WalletBalance balance) {
            WalletBalance balanceAfterWithdrawal = withdrawsMoney(balance.getBalance());
            return balanceAfterWithdrawal;
        }
    }
}
