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
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.server.FreshContextTest;
import io.spine.examples.shareaware.server.given.RejectingPaymentProcess;
import io.spine.examples.shareaware.server.given.WalletTestContext;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletReplenishment;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.event.WalletNotReplenished;
import io.spine.examples.shareaware.wallet.event.WalletReplenished;
import io.spine.server.BoundedContextBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.*;
import static io.spine.examples.shareaware.server.wallet.WalletReplenishmentProcess.*;

@DisplayName("`WalletReplenishment` should")
public final class WalletReplenishmentTest extends FreshContextTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return WalletTestContext.newBuilder();
    }

    @DisplayName("replenish the wallet balance")
    @Nested
    class ReplenishWalletBalance {

        @Test
        @DisplayName("for 1000 USD")
        void entity() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet firstReplenishment = replenish(wallet);
            ReplenishWallet secondReplenishment = replenish(wallet);
            Wallet expectedWallet = walletReplenishedBy(firstReplenishment,
                                                        secondReplenishment,
                                                        wallet);
            context().receivesCommands(firstReplenishment, secondReplenishment);

            context().assertState(wallet, expectedWallet);
        }

        @Test
        @DisplayName("emitting the `BalanceRecharged` event")
        void event() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet command = replenish(wallet);
            BalanceRecharged expected = balanceRechargedBy(command, wallet);
            context().receivesCommand(command);

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("update the `WalletBalance` projection")
    class UpdateWalletBalanceProjection {

        @Test
        @DisplayName("to 1000 USD")
        void balance() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet firstReplenishment = replenish(wallet);
            ReplenishWallet secondReplenishment = replenish(wallet);
            WalletBalance expected = walletBalanceAfterReplenishment(firstReplenishment,
                                                                     secondReplenishment,
                                                                     wallet);
            context().receivesCommands(firstReplenishment, secondReplenishment);

            context().assertState(wallet, expected);
        }
    }

    @Nested
    @DisplayName("be led by `WalletReplenishmentProcess`")
    class ReplenishmentProcess {

        @Test
        @DisplayName("with state")
        void entity() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet command = replenish(wallet);
            WalletReplenishment expectedReplenishment = walletReplenishmentBy(command);
            context().receivesCommand(command);

            context().assertState(command.getReplenishment(), expectedReplenishment);
        }

        @Test
        @DisplayName("which sends the `TransferMoney` command")
        void commandToTransferMoney() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet command = replenish(wallet);
            TransferMoneyFromUser expected =
                    transferMoneyFromUserBy(command, shareAwareIban);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(TransferMoneyFromUser.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which sends the `RechargeBalance` command")
        void commandToRechargeBalance() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet command = replenish(wallet);
            RechargeBalance expected = rechargeBalanceWhen(command);
            context().receivesCommand(command);

            context().assertCommands()
                     .withType(RechargeBalance.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `WalletReplenished` event and archives itself after it")
        void event() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet command = replenish(wallet);
            WalletReplenished event = walletReplenishedAfter(command);
            context().receivesCommand(command);

            context().assertEvent(event);
            context().assertEntity(command.getReplenishment(),
                                   WalletReplenishmentProcess.class)
                     .archivedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("which emits the `WalletNotReplenished` event and archives itself after it")
        void rejection() {
            WalletId wallet = setUpWallet(context());
            ReplenishWallet command = replenish(wallet);
            WalletNotReplenished expected = walletNotReplenishedAfter(command);
            RejectingPaymentProcess.switchToRejectionMode();
            context().receivesCommand(command);

            context().assertEvent(expected);
            context().assertEntity(command.getReplenishment(), WalletReplenishmentProcess.class)
                     .archivedFlag()
                     .isTrue();
            RejectingPaymentProcess.switchToEventsMode();
        }
    }
}
