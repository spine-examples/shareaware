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

import io.spine.examples.shareaware.server.e2e.given.E2EUser;
import io.spine.examples.shareaware.server.e2e.given.WithServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.balanceAfterPurchase;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.investmentAfterPurchase;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.pickTesla;
import static io.spine.examples.shareaware.server.e2e.given.SharePurchaseTestEnv.zeroWalletBalance;

/**
 * End-to-end test that describes such a scenario:
 *
 * <ol>
 *     <li>The user attempts to purchase shares with no money in the wallet.</li>
 *     <li>The user replenishes his wallet for 500 dollars.</li>
 *     <li>The user successfully purchases one 'Tesla' share.</li>
 *     <li>The user withdraws all his money from the wallet.</li>
 * </ol>
 */
final class SharePurchaseTest extends WithServer {

    @Test
    @DisplayName("User should purchase one tesla share and withdraw all the money after this")
    void test() {
        var channel = openChannel();
        var user = new E2EUser(channel);

        var shares = user.looksAtAvailableShares();
        var tesla = pickTesla(shares);

        var failedPurchase = user.purchase(tesla, 1);
        var insufficientFunds = failedPurchase.getB();
        assertThat(insufficientFunds.getAmount()).isEqualTo(tesla.getPrice());

        var balanceAfterReplenishment = user.replenishesWalletFor(usd(500));

        var successfulPurchase = user.purchase(tesla, 1);
        var balanceAfterPurchase = successfulPurchase.getA();
        var investmentInTesla = user.looksAtInvestment();
        var expectedBalance = balanceAfterPurchase(tesla.getPrice(), balanceAfterReplenishment);
        var expectedInvestmentInTesla = investmentAfterPurchase(tesla, user.id());
        assertThat(balanceAfterPurchase).isEqualTo(expectedBalance);
        assertThat(investmentInTesla).isEqualTo(expectedInvestmentInTesla);

        var walletAfterWithdrawal = user.withdrawsAllMoney(balanceAfterPurchase);
        assertThat(walletAfterWithdrawal).isEqualTo(zeroWalletBalance(user.walletId()));
    }
}
