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

package io.spine.examples.shareaware.server.investment;

import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.investment.Investment;
import io.spine.examples.shareaware.investment.command.AddShares;
import io.spine.examples.shareaware.investment.command.CancelSharesReservation;
import io.spine.examples.shareaware.investment.command.CompleteSharesReservation;
import io.spine.examples.shareaware.investment.command.ReserveShares;
import io.spine.examples.shareaware.investment.event.SharesAdded;
import io.spine.examples.shareaware.investment.event.SharesReservationCanceled;
import io.spine.examples.shareaware.investment.event.SharesReservationCompleted;
import io.spine.examples.shareaware.investment.event.SharesReserved;
import io.spine.examples.shareaware.investment.rejection.InsufficientShares;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * Manages the shares of a single type purchased by a particular ShareAware user.
 */
public final class InvestmentAggregate
        extends Aggregate<InvestmentId, Investment, Investment.Builder> {

    @Assign
    SharesAdded on(AddShares c) {
        var newAvailableShares = state().getSharesAvailable() + c.getQuantity();
        return SharesAdded
                .newBuilder()
                .setInvestment(c.getInvestment())
                .setProcess(c.getProcess())
                .setSharesAvailable(newAvailableShares)
                .vBuild();
    }

    @Apply
    private void event(SharesAdded e) {
        builder()
                .setId(e.getInvestment())
                .setSharesAvailable(e.getSharesAvailable());
    }

    @Assign
    SharesReserved on(ReserveShares c) throws InsufficientShares {
        if (state().getSharesAvailable() < c.getQuantity()) {
            throw InsufficientShares
                    .newBuilder()
                    .setInvestment(c.getInvestment())
                    .setProcess(c.getProcess())
                    .setQuantity(c.getQuantity())
                    .build();
        }
        return SharesReserved
                .newBuilder()
                .setInvestment(c.getInvestment())
                .setProcess(c.getProcess())
                .setQuantity(c.getQuantity())
                .vBuild();
    }

    @Apply
    private void event(SharesReserved e) {
        var newAvailableShares = builder().getSharesAvailable() - e.getQuantity();
        var saleId = e.getProcess()
                      .getUuid();
        builder()
                .setSharesAvailable(newAvailableShares)
                .putSharesReserved(saleId, e.getQuantity());
    }

    @Assign
    SharesReservationCompleted on(CompleteSharesReservation c) {
        return SharesReservationCompleted
                .newBuilder()
                .setInvestment(c.getInvestment())
                .setProcess(c.getProcess())
                .setSharesAvailable(state().getSharesAvailable())
                .vBuild();
    }

    @Apply
    private void event(SharesReservationCompleted e) {
        builder().removeSharesReserved(e.getProcess()
                                        .getUuid());
    }

    @Assign
    SharesReservationCanceled on(CancelSharesReservation c) {
        return SharesReservationCanceled
                .newBuilder()
                .setInvestment(c.getInvestment())
                .setProcess(c.getProcess())
                .vBuild();
    }

    @Apply
    private void event(SharesReservationCanceled e) {
        var saleId = e.getProcess()
                      .getUuid();
        var reservedSharesAmount = builder().getSharesReservedOrThrow(saleId);
        var restoredAvailableShares = builder().getSharesAvailable() + reservedSharesAmount;
        builder()
                .setSharesAvailable(restoredAvailableShares)
                .removeSharesReserved(saleId);
    }
}
