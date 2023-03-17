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

package io.spine.examples.shareaware.server.wallet;

import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * The Wallet aggregate is responsible for managing the money
 * of a particular ShareAware user.
 */
public final class WalletAggregate extends Aggregate<WalletId, Wallet, Wallet.Builder> {

    /**
     * Handles the command to create a wallet.
     */
    @Assign
    WalletCreated handle(CreateWallet c) {
        return WalletCreated
                .newBuilder()
                .setWallet(c.getWallet())
                .setBalance(zeroMoneyValue())
                .vBuild();
    }

    @Apply
    private void event(WalletCreated e) {
        builder()
                .setId(e.getWallet())
                .setBalance(e.getBalance())
                .setReservedMoney(zeroMoneyValue());
    }

    private static Money zeroMoneyValue() {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(0)
                .setNanos(0)
                .vBuild();
    }

    @Assign
    BalanceRecharged handle(RechargeBalance c) {
        return BalanceRecharged
                .newBuilder()
                .setWallet(c.getWallet())
                .setMoneyAmount(c.getMoneyAmount())
                .setReplenishmentProcess(c.getReplenishmentProcess())
                .vBuild();
    }

    @Apply
    private void event(BalanceRecharged e) {
        Money rechargedBalance = MoneyCalculator
                .sum(state().getBalance(), e.getMoneyAmount());
        builder()
                .setBalance(rechargedBalance);
    }
}
