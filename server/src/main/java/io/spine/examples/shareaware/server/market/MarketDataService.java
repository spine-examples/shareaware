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

package io.spine.examples.shareaware.server.market;

import io.spine.core.UserId;
import io.spine.examples.shareaware.Share;
import io.spine.examples.shareaware.market.event.MarketSharesUpdated;
import io.spine.server.integration.ThirdPartyContext;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class MarketDataService {

    private static MarketDataService instance = null;

    private static final String tenantName = "Market";

    private final ThirdPartyContext marketContext =
            ThirdPartyContext.singleTenant(tenantName);

    private final ScheduledExecutorService marketThread = newSingleThreadScheduledExecutor();

    private final UserId actor = UserId
            .newBuilder()
            .setValue(tenantName)
            .vBuild();

    /**
     * Prevents instantiation of this class.
     */
    private MarketDataService() {
    }

    public static synchronized MarketDataService instance() {
        if (instance == null) {
            instance = new MarketDataService();
        }
        return instance;
    }

    synchronized void start() {
        marketThread.scheduleAtFixedRate(this::emitEvent, 0, 1, TimeUnit.MINUTES);
    }

    synchronized void stop() {
        marketThread.shutdown();
    }

    private void emitEvent() {
        List<Share> actualShares = MarketData.actualShares();
        MarketSharesUpdated event = MarketSharesUpdated
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .addAllShare(actualShares)
                .vBuild();
        marketContext.emittedEvent(event, actor);
    }
}
