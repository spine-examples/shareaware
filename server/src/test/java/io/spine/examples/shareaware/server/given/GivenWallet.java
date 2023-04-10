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

package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.examples.shareaware.server.given.GivenMoney.*;

public class GivenWallet {

    public static final Iban USERS_IBAN = Iban
            .newBuilder()
            .setValue("FI211234569876543210")
            .vBuild();

    /**
     * Generates {@code ReplenishWallet} command.
     */
    public static ReplenishWallet replenishWith(Money amount, WalletId wallet) {
        ReplenishmentId replenishment = ReplenishmentId.generate();
        return ReplenishWallet
                .newBuilder()
                .setWallet(wallet)
                .setReplenishment(replenishment)
                .setIban(USERS_IBAN)
                .setMoneyAmount(amount)
                .vBuild();
    }

    /**
     * Creates a {@code Wallet} in {@code context} by sending {@code CreateWallet} command to it.
     *
     * @return the ID of created {@code Wallet}.
     */
    public static WalletId setUpWallet(BlackBoxContext context) {
        CreateWallet command = createWallet();
        context.receivesCommand(command);
        return command.getWallet();
    }

    public static Wallet setUpReplenishedWallet(BlackBoxContext context) {
        WalletId wallet = setUpWallet(context);
        ReplenishWallet command = replenishWith(usd(500), wallet);
        context.receivesCommand(command);
        return Wallet
                .newBuilder()
                .setId(wallet)
                .setBalance(command.getMoneyAmount())
                .vBuild();
    }

    private static WalletId givenId() {
        return WalletId
                .newBuilder()
                .setOwner(GivenUserId.generated())
                .vBuild();
    }

    private static CreateWallet createWallet() {
        return CreateWallet
                .newBuilder()
                .setWallet(givenId())
                .vBuild();
    }
}
