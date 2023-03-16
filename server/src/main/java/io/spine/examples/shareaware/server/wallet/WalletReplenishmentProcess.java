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
import io.spine.examples.shareaware.payment_gateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.payment_gateway.event.MoneyTransferredFromUser;
import io.spine.examples.shareaware.server.payment_gateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.WalletReplenishment;
import io.spine.examples.shareaware.wallet.command.RechargeBalance;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.examples.shareaware.wallet.replenishment_command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.replenishment_event.WalletReplenished;
import io.spine.server.command.Command;
import io.spine.server.event.React;
import io.spine.server.procman.ProcessManager;

final class WalletReplenishmentProcess
        extends ProcessManager<ReplenishmentId, WalletReplenishment, WalletReplenishment.Builder> {

    static final Iban shareAwareIban = Iban
            .newBuilder()
            .setValue("UA227452600000367849267457823")
            .vBuild();

    @Command
    TransferMoneyFromUser on(ReplenishWallet c) {
        initState(c);
        return TransferMoneyFromUser
                .newBuilder()
                .setGateway(PaymentGatewayProcess.id)
                .setReplenishmentProcess(c.getReplenishment())
                .setRecipient(shareAwareIban)
                .setSender(c.getIban())
                .setTransactionAmount(c.getMoneyAmount())
                .vBuild();
    }

    private void initState(ReplenishWallet c) {
        builder()
                .setId(c.getReplenishment())
                .setWallet(c.getWallet());
    }

    @Command
    RechargeBalance on(MoneyTransferredFromUser e) {
        return RechargeBalance
                .newBuilder()
                .setWallet(state().getWallet())
                .setReplenishmentProcess(state().getId())
                .setMoneyAmount(e.getTransactionAmount())
                .vBuild();
    }

    @React
    WalletReplenished on(BalanceRecharged e) {
        setArchived(true);
        return WalletReplenished
                .newBuilder()
                .setReplenishment(state().getId())
                .setWallet(e.getWallet())
                .setMoneyAmount(e.getMoneyAmount())
                .vBuild();
    }
}
