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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Provides data about currently available shares on the market to the main ShareAware context.
 */
public class MarketDataService {

    private final AtomicBoolean isActive = new AtomicBoolean();

    private static MarketDataService instance = null;

    private static final String tenantName = "MarketData";

    private final ThirdPartyContext marketContext =
            ThirdPartyContext.singleTenant(tenantName);

    private final ExecutorService marketThread = newSingleThreadExecutor();

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

    /**
     * Emits the {@code MarketSharesUpdated} event every 4 seconds on behalf of {@code ThirdPartyContext}.
     */
    synchronized void start() {
        isActive.set(true);
        marketThread.execute(() -> {
            while (isActive.get()) {
                sleepUninterruptibly(Duration.ofSeconds(4));
                emitEvent();
            }
        });
    }

    /**
     * Stops the event emission.
     */
    synchronized void stop() {
        isActive.set(false);
        marketThread.shutdown();
    }

    private void emitEvent() {
        List<Share> updatedShares = MarketData.actualShares();
        MarketSharesUpdated event = MarketSharesUpdated
                .newBuilder()
                .setMarket(MarketProcess.ID)
                .addAllShare(updatedShares)
                .vBuild();
        marketContext.emittedEvent(event, actor);
    }
}
