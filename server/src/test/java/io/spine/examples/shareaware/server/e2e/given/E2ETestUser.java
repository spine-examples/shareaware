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
import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.base.EventMessage;
import io.spine.client.Client;
import io.spine.client.Subscription;
import io.spine.core.UserId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.server.given.GivenWallet;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.testing.core.given.GivenUserId;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a user for end-to-end tests which interacts
 * with the server-side with the help of {@link Client}.
 *
 * <p>In case when the user is needed to have actions that will describe the test scenario,
 * the inheritor must be defined as an inner private class in the test class
 * for test readability improvement.
 */
public class E2ETestUser {

    private final Client client;
    private final UserId userId;
    private final WalletId walletId;

    public E2ETestUser(Client client) {
        this.client = client;
        this.userId = GivenUserId.generated();
        this.walletId = GivenWallet.walletId(userId);
        CreateWallet createWallet = CreateWallet
                .newBuilder()
                .setWallet(walletId)
                .vBuild();
        client.onBehalfOf(userId)
              .command(createWallet)
              .postAndForget();
    }

    /**
     * Allows user to take a look at all the {@code EntityState}s with the provided type.
     */
    public <S extends EntityState> ImmutableList<S> lookAt(Class<S> type) {
        return client
                .onBehalfOf(userId)
                .select(type)
                .run();
    }

    /**
     * Allows user to send the provided command to the server.
     */
    public void command(CommandMessage commandMessage) {
        client.onBehalfOf(userId)
              .command(commandMessage)
              .postAndForget();
    }

    /**
     * Subscribes the user to receive the event of the passed type.
     *
     * <p>Returns only the {@link CompletableFuture} that stores the event
     * without {@link Subscription}.
     *
     * @see E2ETestUser#subscribeToEvent(Class)
     */
    protected <E extends EventMessage> CompletableFuture<E>
    subscribeToEventAndForget(Class<E> type) {
        SubscriptionOutcome<E> subscriptionOutcome = subscribeToEvent(type);
        return subscriptionOutcome.future();
    }

    /**
     * Subscribes the user on changes of the passed type of the {@code EntityState}.
     *
     * <p>Returns only the {@link CompletableFuture} that stores the {@code EntityState}
     * without {@link Subscription}.
     */
    protected <S extends EntityState> CompletableFuture<S>
    subscribeToStateAndForget(Class<S> type) {
        SubscriptionOutcome<S> subscriptionOutcome = subscribeToState(type);
        return subscriptionOutcome.future();
    }

    /**
     * Subscribes the user to receive the event of the passed type.
     */
    protected <S extends EventMessage> SubscriptionOutcome<S> subscribeToEvent(Class<S> type) {
        CompletableFuture<S> future = new CompletableFuture<>();
        Subscription subscription = client
                .onBehalfOf(userId)
                .subscribeToEvent(type)
                .observe(future::complete)
                .post();
        return new SubscriptionOutcome<>(future, subscription);
    }

    /**
     * Subscribes the user on changes of the passed type of the {@code EntityState}.
     */
    protected <S extends EntityState> SubscriptionOutcome<S> subscribeToState(Class<S> type) {
        CompletableFuture<S> future = new CompletableFuture<>();
        Subscription subscription = client
                .onBehalfOf(userId)
                .subscribeTo(type)
                .observe(future::complete)
                .post();
        return new SubscriptionOutcome<>(future, subscription);
    }

    /**
     * Cancels the passed subscription.
     *
     * @see io.spine.client.Subscriptions#cancel(Subscription)
     */
    protected void cancel(Subscription subscription) {
        client.subscriptions()
              .cancel(subscription);
    }

    public UserId id() {
        return userId;
    }

    public WalletId walletId() {
        return walletId;
    }
}
