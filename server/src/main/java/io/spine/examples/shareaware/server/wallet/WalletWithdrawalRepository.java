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

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.paymentgateway.event.MoneyTransferredToUser;
import io.spine.examples.shareaware.wallet.WalletWithdrawal;
import io.spine.examples.shareaware.wallet.event.MoneyReservationCanceled;
import io.spine.examples.shareaware.wallet.event.MoneyReserved;
import io.spine.examples.shareaware.wallet.event.ReservedMoneyDebited;
import io.spine.server.procman.ProcessManagerRepository;
import io.spine.server.route.EventRouting;

import static io.spine.server.route.EventRoute.*;

/**
 * Manages  instances of {@link WalletWithdrawalProcess}.
 */
public class WalletWithdrawalRepository
        extends ProcessManagerRepository<WithdrawalId, WalletWithdrawalProcess, WalletWithdrawal> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<WithdrawalId> routing) {
        super.setupEventRouting(routing);
        routing.route(MoneyTransferredToUser.class,
                      (event, context) -> withId(event.getWithdrawalProcess()));
        routing.route(MoneyReserved.class,
                      (event, context) -> withId(event.getWithdrawalProcess()));
        routing.route(ReservedMoneyDebited.class,
                      (event, context) -> withId(event.getWithdrawalProcess()));
        routing.route(MoneyReservationCanceled.class,
                      (event, context) -> withId(event.getWithdrawalProcess()));
    }
}