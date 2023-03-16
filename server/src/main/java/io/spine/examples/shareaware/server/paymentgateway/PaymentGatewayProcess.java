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

package io.spine.examples.shareaware.server.paymentgateway;

import io.spine.examples.shareaware.PaymentGatewayId;
import io.spine.examples.shareaware.payment_gateway.PaymentGateway;
import io.spine.examples.shareaware.payment_gateway.command.TransferMoneyFromUser;
import io.spine.examples.shareaware.payment_gateway.event.MoneyTransferredFromUser;
import io.spine.server.command.Assign;
import io.spine.server.procman.ProcessManager;

// The imitation of the external payment system.
public class PaymentGatewayProcess extends ProcessManager<PaymentGatewayId, PaymentGateway, PaymentGateway.Builder> {

    // The hardcoded ID for this imitation.
    public static final PaymentGatewayId id = PaymentGatewayId
            .newBuilder()
            .setUuid("ImitationOfExternalPaymentSystem")
            .vBuild();

    // Emits the event when the transaction was successful.
    @Assign
    MoneyTransferredFromUser on(TransferMoneyFromUser c) {
        return MoneyTransferredFromUser
                .newBuilder()
                .setGateway(c.getGateway())
                .setReplenishmentProcess(c.getReplenishmentProcess())
                .setTransactionAmount(c.getTransactionAmount())
                .vBuild();
    }
}
