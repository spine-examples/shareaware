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

package io.spine.examples.shareaware.server.e2e.given;

import io.grpc.ManagedChannel;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.market.MarketDataProvider;
import io.spine.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;

import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.spine.server.Server.atPort;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * An abstract base for tests that need a gRPC server.
 */
public abstract class WithServer {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 4242;
    private Server server;
    private final Collection<ManagedChannel> channels = new ArrayList<>();
    private static final MarketDataProvider provider = MarketDataProvider.instance();

    /**
     * Runs the {@code MarketDataProvider} to provide data about available shares on the market.
     */
    @BeforeAll
    static void startProvider() {
        provider.runWith(Duration.ofSeconds(1));
    }

    /**
     * Stops the {@code MarketDataProvider}.
     */
    @AfterAll
    static void stopProvider() {
        provider.stopEmission();
    }

    /**
     * Starts the server.
     */
    @BeforeEach
    void startAndConnect() throws IOException {
        server = atPort(PORT)
                .add(TradingContext.newBuilder())
                .build();
        server.start();
    }

    /**
     * Shuts the server and all channels down.
     */
    @AfterEach
    void stopAndDisconnect() {
        server.shutdown();
        channels.forEach(channel -> {
                             channel.shutdown();
                             try {
                                 channel.awaitTermination(1, SECONDS);
                             } catch (InterruptedException e) {
                                 throw illegalStateWithCauseOf(e);
                             }
                         }
        );
    }

    /**
     * Opens a chanel.
     */
    protected ManagedChannel openChannel() {
        ManagedChannel channel = forAddress(ADDRESS, PORT)
                .usePlaintext()
                .build();
        channels.add(channel);
        return channel;
    }
}
