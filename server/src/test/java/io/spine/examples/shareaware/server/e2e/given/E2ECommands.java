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
import io.spine.examples.shareaware.PurchaseId;
import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.investment.command.PurchaseShares;
import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
import io.spine.money.Money;

import static io.spine.examples.shareaware.server.given.GivenWallet.userIban;

/**
 * Factory methods for creating the main commands of ShareAware application
 * for end-to-end test purposes.
 */
final class E2ECommands {

    /**
     * Prevents instantiation of this class.
     */
    private E2ECommands() {
    }

    static ReplenishWallet replenishWallet(WalletId id, Money amount) {
        return ReplenishWallet
                .newBuilder()
                .setWallet(id)
                .setReplenishment(ReplenishmentId.generate())
                .setIban(userIban())
                .setMoneyAmount(amount)
                .vBuild();
    }

    static PurchaseShares purchaseSharesFor(UserId user,
                                            Share share,
                                            int quantity) {
        return PurchaseShares
                .newBuilder()
                .setPurchaser(user)
                .setPurchaseProcess(PurchaseId.generate())
                .setQuantity(quantity)
                .setShare(share.getId())
                .setPrice(share.getPrice())
                .vBuild();
    }

    static WithdrawMoney withdrawMoneyFrom(WalletId wallet, Money amount) {
        return WithdrawMoney
                .newBuilder()
                .setWithdrawalProcess(WithdrawalId.generate())
                .setWallet(wallet)
                .setRecipient(userIban())
                .setAmount(amount)
                .vBuild();
    }
}
