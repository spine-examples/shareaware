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
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.investment.InvestmentView;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.money.Money;

import java.util.Collection;

import static io.spine.examples.shareaware.MoneyCalculator.subtract;
import static io.spine.examples.shareaware.given.GivenMoney.zero;
import static io.spine.util.Exceptions.newIllegalArgumentException;

public final class SharePurchaseTestEnv {

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

    static WalletBalance walletBalanceWith(Money amount, WalletId walletId) {
        return WalletBalance
                .newBuilder()
                .setId(walletId)
                .setBalance(amount)
                .vBuild();
    }

    public static WalletBalance balanceAfterPurchase(Money sharePrice,
                                                     WalletBalance balance) {
        var reducedBalance = subtract(balance.getBalance(), sharePrice);
        return balance
                .toBuilder()
                .setBalance(reducedBalance)
                .vBuild();
    }

    public static InvestmentView investmentAfterPurchase(Share tesla, UserId user) {
        var id = investmentId(user, tesla.getId());
        return InvestmentView
                .newBuilder()
                .setId(id)
                .setSharesAvailable(1)
                .vBuild();
    }

    private static InvestmentId investmentId(UserId user, ShareId share) {
        return InvestmentId
                .newBuilder()
                .setOwner(user)
                .setShare(share)
                .vBuild();
    }

    public static Share pickTesla(Collection<Share> shares) {
        var tesla = shares
                .stream()
                .filter(share -> share.getCompanyName()
                                       .toLowerCase()
                                       .contains("tesla"))
                .findAny();
        if (tesla.isPresent()) {
            return tesla.get();
        }
        throw newIllegalArgumentException("No 'Tesla' share were found in the provided `Collection`.");
    }
}
