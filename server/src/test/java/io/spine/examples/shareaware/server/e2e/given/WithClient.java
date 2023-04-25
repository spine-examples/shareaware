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

import com.google.common.collect.ImmutableList;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.EventMessage;
import io.spine.client.Client;
import io.spine.client.Subscription;
import io.spine.core.UserId;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.server.market.MarketDataProvider;
import io.spine.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static io.grpc.ManagedChannelBuilder.forAddress;
import static io.grpc.Status.CANCELLED;
import static io.spine.client.Client.usingChannel;
import static io.spine.server.Server.atPort;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class WithClient {

    private static final String ADDRESS = "localhost";
    private static final int PORT = 4242;
    private Client client;
    private Server server;
    private ManagedChannel channel;
    private static final MarketDataProvider provider = MarketDataProvider.instance();

    @BeforeAll
    static void startProvider() {
        provider.runWith(Duration.ofSeconds(1));
    }

    @AfterAll
    static void stopProvider() {
        provider.stop();
    }

    @BeforeEach
    void startAndConnect() throws IOException {
        channel = forAddress(ADDRESS, PORT)
                .usePlaintext()
                .build();
        server = atPort(PORT)
                .add(TradingContext.newBuilder())
                .build();
        server.start();
        client = usingChannel(channel).build();
    }

    @AfterEach
    void stopAndDisconnect() throws InterruptedException {
        try {
            client.close();
        } catch (StatusRuntimeException e) {
            if (e.getStatus()
                 .equals(CANCELLED)) {
                fail(e);
            }
        }
        server.shutdown();
        channel.shutdown();
        channel.awaitTermination(1, SECONDS);
    }

    protected Client client() {
        return client;
    }

    protected <E extends EventMessage> CompletableFuture<E> subscribeToEventAndForget(Class<E> type,
                                                                                      UserId user) {
        FutureAndSubscription<E> futureAndSubscription = subscribeToEvent(type, user);
        return futureAndSubscription.future();
    }

    protected <S extends EntityState> CompletableFuture<S> subscribeToStateAndForget(Class<S> type,
                                                                                     UserId user) {
        FutureAndSubscription<S> futureAndSubscription = subscribeToState(type, user);
        return futureAndSubscription.future();
    }

    protected <S extends EventMessage> FutureAndSubscription<S> subscribeToEvent(Class<S> type,
                                                                                 UserId user) {
        CompletableFuture<S> future = new CompletableFuture<>();
        Subscription subscription = client
                .onBehalfOf(user)
                .subscribeToEvent(type)
                .observe(future::complete)
                .post();
        return new FutureAndSubscription<>(future, subscription);
    }

    protected <S extends EntityState> FutureAndSubscription<S> subscribeToState(Class<S> type,
                                                                                UserId user) {
        CompletableFuture<S> future = new CompletableFuture<>();
        Subscription subscription = client
                .onBehalfOf(user)
                .subscribeTo(type)
                .observe(future::complete)
                .post();
        return new FutureAndSubscription<>(future, subscription);
    }

    protected void command(CommandMessage commandMessage, UserId user) {
        client.onBehalfOf(user)
              .command(commandMessage)
              .postAndForget();
    }

    protected void cancel(Subscription subscription) {
        client.subscriptions()
              .cancel(subscription);
    }

    protected <S extends EntityState> ImmutableList<S> lookAt(Class<S> type,
                                                              UserId user) {
        return client
                .onBehalfOf(user)
                .select(type)
                .run();
    }
}
