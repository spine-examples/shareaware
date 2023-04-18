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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Provides data about currently available shares on the market to the ShareAware context.
 *
 * @implNote It is not allowed to stop and then start the provider again.
 */
public final class MarketDataProvider {

    @MonotonicNonNull
    private static MarketDataProvider instance;

    /**
     * The name of the Bounded Context on behalf of which the data
     * about available shares on the market will be provided.
     */
    private static final String tenantName = "MarketData";

    /**
     * A single-tenant instance of the {@code ThirdPartyContext}
     * that represents {@value tenantName} as a Bounded Context to the ShareAware application.
     */
    private final ThirdPartyContext marketContext =
            ThirdPartyContext.singleTenant(tenantName);

    /**
     * The actor on whose behalf the {@code MarketSharesUpdated} event is emitted.
     */
    private final UserId actor = UserId
            .newBuilder()
            .setValue(tenantName)
            .vBuild();

    /**
     * The thread executor that allows {@code MarketDataProvider} to work
     * in a separate thread from the ShareAware application.
     */
    private final ExecutorService marketThread = newSingleThreadExecutor();

    /**
     * Controls the thread in which {@code MarketDataProvider}
     * is executed to be active or not.
     */
    private final AtomicBoolean active = new AtomicBoolean();

    /**
     * Prevents instantiation of this class.
     */
    private MarketDataProvider() {
    }

    /**
     * Creates the instance of the {@code MarketDataProvider} if there is no such,
     * otherwise returns the existing.
     */
    public static synchronized MarketDataProvider instance() {
        if (instance == null) {
            instance = new MarketDataProvider();
        }
        return instance;
    }

    /**
     * Emits the {@code MarketSharesUpdated} event with a specified periodicity
     * on behalf of the {@value tenantName} Bounded Context.
     */
    synchronized void runWith(Duration period) {
        active.set(true);
        marketThread.execute(() -> {
            while (active.get()) {
                sleepUninterruptibly(period);
                emitEvent();
            }
        });
    }

    /**
     * Stops the event emission.
     */
    synchronized void stop() {
        active.set(false);
        marketThread.shutdownNow();
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
