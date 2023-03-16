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

import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.payment_gateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.payment_gateway.rejection.Rejections.MoneyCannotBeTransferredFromUser;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.given.GivenMoney;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.WalletReplenishment;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.replenishment_command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.replenishment_event.WalletNotReplenished;
import io.spine.examples.shareaware.wallet.replenishment_event.WalletReplenished;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.server.given.WalletTestEnv.replenishWallet;
import static io.spine.examples.shareaware.server.given.WalletTestEnv.setupWallet;

@DisplayName("`WalletReplenishment` should")
public class WalletReplenishmentTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingContext.newBuilder();
    }

    @DisplayName("replenish the wallet balance")
    @Nested
    class ReplenishWalletBalance {

        @Test
        @DisplayName("for 1000 USD")
        void entity() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishWallet firstReplenishment = replenishWallet(wallet,
                                                                 ReplenishmentId.generate(),
                                                                 replenishmentAmount);
            ReplenishWallet secondReplenishment = replenishWallet(wallet,
                                                                  ReplenishmentId.generate(),
                                                                  replenishmentAmount);
            Money expectedBalance = MoneyCalculator.summarize(replenishmentAmount,
                                                              replenishmentAmount);
            Wallet expectedWallet = Wallet
                    .newBuilder()
                    .setId(wallet)
                    .setBalance(expectedBalance)
                    .vBuild();
            context().receivesCommands(firstReplenishment, secondReplenishment);

            context().assertState(wallet, expectedWallet);
        }

        @Test
        @DisplayName("emitting the `BalanceRecharged` event")
        void event() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishmentId replenishment = ReplenishmentId.generate();
            ReplenishWallet command = replenishWallet(wallet,
                                                      replenishment,
                                                      replenishmentAmount);
            context().receivesCommand(command);
            BalanceRecharged expected = BalanceRecharged
                    .newBuilder()
                    .setWallet(wallet)
                    .setMoneyAmount(replenishmentAmount)
                    .setReplenishmentProcess(replenishment)
                    .vBuild();

            context().assertEvent(expected);
        }
    }

    @Nested
    @DisplayName("update the `WalletBalance` projection")
    class UpdateWalletBalanceProjection {

        @Test
        @DisplayName("to 1000 USD")
        void balance() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishmentId replenishment = ReplenishmentId.generate();
            ReplenishWallet firstReplenishment = replenishWallet(wallet,
                                                                 ReplenishmentId.generate(),
                                                                 replenishmentAmount);
            ReplenishWallet secondReplenishment = replenishWallet(wallet,
                                                                  ReplenishmentId.generate(),
                                                                  replenishmentAmount);
            Money expectedBalance = MoneyCalculator.summarize(replenishmentAmount,
                                                              replenishmentAmount);
            WalletBalance expected = WalletBalance
                    .newBuilder()
                    .setId(wallet)
                    .setBalance(expectedBalance)
                    .vBuild();
            context().receivesCommands(firstReplenishment, secondReplenishment);
        }
    }

    @Nested
    @DisplayName("be led by `WalletReplenishmentProcess`")
    class Process {

        @Test
        @DisplayName("with state")
        void entity() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishmentId replenishment = ReplenishmentId.generate();
            ReplenishWallet replenishWalletCommand = replenishWallet(wallet,
                                                                     replenishment,
                                                                     replenishmentAmount);
            WalletReplenishment expectedReplenishment = WalletReplenishment
                    .newBuilder()
                    .setWallet(wallet)
                    .setId(replenishment)
                    .vBuild();
            context().receivesCommand(replenishWalletCommand);

            context().assertState(replenishment, expectedReplenishment);
        }

        @Test
        @DisplayName("which sends the `TransferMoney` command")
        void commandToTransferMoney() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishmentId replenishment = ReplenishmentId.generate();
            ReplenishWallet command = replenishWallet(wallet,
                                                      replenishment,
                                                      replenishmentAmount);
            context().receivesCommand(command);
            TransferMoneyFromUser expected = TransferMoneyFromUser
                    .newBuilder()
                    .setGateway(PaymentGatewayProcess.id)
                    .setReplenishmentProcess(replenishment)
                    .setTransactionAmount(replenishmentAmount)
                    .setSender(command.getIban())
                    .setRecipient(WalletReplenishmentProcess.shareAwareIban)
                    .vBuild();

            context().assertCommands()
                     .withType(TransferMoneyFromUser.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which sends the `RechargeBalance` command")
        void commandToRechargeBalance() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishmentId replenishment = ReplenishmentId.generate();
            ReplenishWallet command = replenishWallet(wallet,
                                                      replenishment,
                                                      replenishmentAmount);
            context().receivesCommand(command);
            RechargeBalance expected = RechargeBalance
                    .newBuilder()
                    .setWallet(wallet)
                    .setReplenishmentProcess(replenishment)
                    .setMoneyAmount(replenishmentAmount)
                    .vBuild();

            context().assertCommands()
                     .withType(RechargeBalance.class)
                     .message(0)
                     .isEqualTo(expected);
        }

        @Test
        @DisplayName("which emits the `WalletReplenished` event and archives itself after it")
        void event() {
            WalletId wallet = setupWallet(context());
            Money replenishmentAmount = GivenMoney.generatedWith(500, Currency.USD);
            ReplenishmentId replenishment = ReplenishmentId.generate();
            ReplenishWallet replenishWalletCommand = replenishWallet(wallet,
                                                                     replenishment,
                                                                     replenishmentAmount);

            WalletReplenished event = WalletReplenished
                    .newBuilder()
                    .setReplenishment(replenishment)
                    .setWallet(wallet)
                    .setMoneyAmount(replenishmentAmount)
                    .vBuild();
            context().receivesCommand(replenishWalletCommand);

            context().assertEvent(event);
            context().assertEntity(replenishment, WalletReplenishmentProcess.class)
                     .archivedFlag()
                     .isTrue();
        }

        @Test
        @DisplayName("which emits the `WalletNotReplenished` event and archives itself after it")
        void rejection() {
            ReplenishmentId replenishment = ReplenishmentId.generate();
            String cause = "Cause of the rejection";
            MoneyCannotBeTransferredFromUser rejection = MoneyCannotBeTransferredFromUser
                    .newBuilder()
                    .setReplenishment(replenishment)
                    .setCause(cause)
                    .vBuild();
            WalletNotReplenished expected = WalletNotReplenished
                    .newBuilder()
                    .setReplenishment(replenishment)
                    .setCause(cause)
                    .vBuild();
            context().receivesEvent(rejection);

            context().assertEvent(expected);
            context().assertEntity(replenishment, WalletReplenishmentProcess.class)
                     .archivedFlag()
                     .isTrue();
        }
    }
}
