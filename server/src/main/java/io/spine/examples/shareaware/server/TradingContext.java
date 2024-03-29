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

package io.spine.examples.shareaware.server;

import io.spine.examples.shareaware.server.investment.InvestmentViewRepository;
import io.spine.examples.shareaware.server.investment.InvestmentAggregate;
import io.spine.examples.shareaware.server.investment.SharesPurchaseRepository;
import io.spine.examples.shareaware.server.investment.SharesSaleRepository;
import io.spine.examples.shareaware.server.market.AvailableMarketSharesRepository;
import io.spine.examples.shareaware.server.market.MarketProcess;
import io.spine.examples.shareaware.server.paymentgateway.PaymentGatewayProcess;
import io.spine.examples.shareaware.server.wallet.WalletAggregate;
import io.spine.examples.shareaware.server.wallet.WalletBalanceRepository;
import io.spine.examples.shareaware.server.wallet.WalletReplenishmentRepository;
import io.spine.examples.shareaware.server.wallet.WalletWithdrawalRepository;
import io.spine.examples.shareaware.server.watchlist.UserWatchlistsRepository;
import io.spine.examples.shareaware.server.watchlist.WatchlistAggregate;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.DefaultRepository;

/**
 * Configures Trading Bounded Context with repositories.
 */
public final class TradingContext {

    static final String NAME = "Trading";

    /**
     * Prevents instantiation of this class.
     */
    private TradingContext() {
    }

    /**
     * Creates {@code BoundedContextBuilder} for the Trading context
     * and fills it with repositories.
     */
    public static BoundedContextBuilder newBuilder() {
        return BoundedContext
                .singleTenant(NAME)
                .add(DefaultRepository.of(WatchlistAggregate.class))
                .add(DefaultRepository.of(WalletAggregate.class))
                .add(DefaultRepository.of(InvestmentAggregate.class))
                .add(DefaultRepository.of(PaymentGatewayProcess.class))
                .add(DefaultRepository.of(MarketProcess.class))
                .add(new WalletWithdrawalRepository())
                .add(new WalletReplenishmentRepository())
                .add(new WalletBalanceRepository())
                .add(new UserWatchlistsRepository())
                .add(new SharesPurchaseRepository())
                .add(new SharesSaleRepository())
                .add(new InvestmentViewRepository())
                .add(new AvailableMarketSharesRepository());
    }
}
