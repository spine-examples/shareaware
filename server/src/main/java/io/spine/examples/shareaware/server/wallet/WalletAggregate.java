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
import io.spine.examples.shareaware.wallet.command.CancelMoneyReservation;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.DebitReservedMoney;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.command.ReserveMoney;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.examples.shareaware.wallet.rejection.InsufficientFunds;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

import static io.spine.examples.shareaware.MoneyCalculator.*;

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
                .setBalance(e.getBalance());
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
        var newBalance = sum(state().getBalance(), c.getMoneyAmount());
        return BalanceRecharged
                .newBuilder()
                .setWallet(c.getWallet())
                .setCurrentBalance(newBalance)
                .setOperation(c.getOperation())
                .vBuild();
    }

    @Apply
    private void event(BalanceRecharged e) {
        builder().setBalance(e.getCurrentBalance());
    }

    @Assign
    MoneyReserved on(ReserveMoney c) throws InsufficientFunds {
        if (isGreater(c.getAmount(), state().getBalance())) {
            throw InsufficientFunds
                    .newBuilder()
                    .setWallet(c.getWallet())
                    .setOperation(c.getOperation())
                    .setAmount(c.getAmount())
                    .build();
        }
        return MoneyReserved
                .newBuilder()
                .setWallet(c.getWallet())
                .setOperation(c.getOperation())
                .setAmount(c.getAmount())
                .vBuild();
    }

    @Apply
    private void event(MoneyReserved e) {
        var newBalance = subtract(builder().getBalance(), e.getAmount());
        var operationId = e.operationIdValue();
        builder()
                .setBalance(newBalance)
                .putReservedMoney(operationId, e.getAmount());
    }

    @Assign
    ReservedMoneyDebited on(DebitReservedMoney c) {
        return ReservedMoneyDebited
                .newBuilder()
                .setOperation(c.getOperation())
                .setWallet(c.getWallet())
                .setCurrentBalance(state().getBalance())
                .vBuild();
    }

    @Apply
    private void event(ReservedMoneyDebited e) {
        var operationId = e.operationIdValue();
        builder().removeReservedMoney(operationId);
    }

    @Assign
    MoneyReservationCanceled on(CancelMoneyReservation e) {
        return MoneyReservationCanceled
                .newBuilder()
                .setOperation(e.getOperation())
                .setWallet(e.getWallet())
                .vBuild();
    }

    @Apply
    private void event(MoneyReservationCanceled e) {
        var operationId = e.operationIdValue();
        var reservedAmount = builder().getReservedMoneyOrThrow(operationId);
        var restoredBalance = sum(builder().getBalance(), reservedAmount);
        builder()
                .setBalance(restoredBalance)
                .removeReservedMoney(operationId);
    }
}
