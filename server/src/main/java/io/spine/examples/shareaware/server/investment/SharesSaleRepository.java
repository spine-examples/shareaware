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

import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.examples.shareaware.ReplenishmentOperationId;
import io.spine.examples.shareaware.SaleId;
import io.spine.examples.shareaware.investment.SharesSale;
import io.spine.examples.shareaware.investment.event.SharesReservationCanceled;
import io.spine.examples.shareaware.investment.event.SharesReservationCompleted;
import io.spine.examples.shareaware.investment.event.SharesReserved;
import io.spine.examples.shareaware.investment.rejection.Rejections.InsufficientShares;
import io.spine.examples.shareaware.market.event.SharesSoldOnMarket;
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeSoldOnMarket;
import io.spine.examples.shareaware.wallet.event.BalanceRecharged;
import io.spine.server.procman.ProcessManagerRepository;
import io.spine.server.route.EventRouting;

import java.util.Set;

import static io.spine.server.route.EventRoute.*;

/**
 * Manages instances of {@link SharesSaleProcess}.
 */
public final class SharesSaleRepository
        extends ProcessManagerRepository<SaleId, SharesSaleProcess, SharesSale> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<SaleId> routing) {
        super.setupEventRouting(routing);
        routing.route(SharesReserved.class,
                      (event, context) -> withId(event.getProcess()))
               .route(InsufficientShares.class,
                      (event, context) -> withId(event.getProcess()))
               .route(SharesSoldOnMarket.class,
                      (event, context) -> withId(event.getSaleProcess()))
               .route(SharesCannotBeSoldOnMarket.class,
                      (event, context) -> withId(event.getSaleProcess()))
               .route(SharesReservationCanceled.class,
                      (event, context) -> withId(event.getProcess()))
               .route(BalanceRecharged.class,
                      (event, context) -> withSaleId(event.getOperation()))
               .route(SharesReservationCompleted.class,
                      (event, context) -> withId(event.getProcess()));
    }

    private static Set<SaleId> withSaleId(ReplenishmentOperationId id) {
        if (id.hasSale()) {
            return ImmutableSet.of(id.getSale());
        }
        return ImmutableSet.of();
    }
}
