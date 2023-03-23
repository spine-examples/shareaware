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

import io.spine.examples.shareaware.PaymentGatewayId;
import io.spine.examples.shareaware.paymentgateway.PaymentGateway;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.paymentgateway.command.TransferMoneyToUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredFromUser;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.paymentgateway.rejection.MoneyCannotBeTransferredFromUser;
import io.spine.examples.shareaware.paymentgateway.rejection.MoneyCannotBeTransferredToUser;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;

/**
 * The test imitation of {@code PaymentGatewayProcess} with rejection mode.
 *
 * <p>When {@code RejectControllablePaymentSystem} is in rejection mode,
 * it will reject all commands directed at it and vice versa.
 */
public final class RejectControllablePaymentGatewayProcess
        extends ProcessManager<PaymentGatewayId, PaymentGateway, PaymentGateway.Builder> {

    private static boolean rejectionMode = false;

    /**
     * Emits the {@code TransferMoneyFromUser} event when rejection mode if off
     * otherwise throws the {@code MoneyCannotBeTransferredFromUser} rejection.
     */
    @Assign
    MoneyTransferredFromUser on(TransferMoneyFromUser c) throws MoneyCannotBeTransferredFromUser {
        if (rejectionMode) {
            throw MoneyCannotBeTransferredFromUser
                    .newBuilder()
                    .setReplenishment(c.getReplenishmentProcess())
                    .build();
        }
        return MoneyTransferredFromUser
                .newBuilder()
                .setGateway(c.getGateway())
                .setReplenishmentProcess(c.getReplenishmentProcess())
                .setAmount(c.getAmount())
                .vBuild();
    }

    /**
     * Emits the {@code MoneyTransferredToUser} event when rejection mode if off
     * otherwise throws the {@code MoneyCannotBeTransferredToUser} rejection.
     */
    @Assign
    MoneyTransferredToUser on(TransferMoneyToUser c) throws MoneyCannotBeTransferredToUser {
        if (rejectionMode) {
            throw MoneyCannotBeTransferredToUser
                    .newBuilder()
                    .setWithdrawalProcess(c.getWithdrawalProcess())
                    .setCause("")
                    .build();
        }
        return MoneyTransferredToUser
                .newBuilder()
                .setGetaway(c.getGateway())
                .setWithdrawalProcess(c.getWithdrawalProcess())
                .setAmount(c.getAmount())
                .vBuild();
    }

    public static void switchToRejectionMode() {
        rejectionMode = true;
    }

    public static void switchToEventsMode() {
        rejectionMode = false;
    }
}
