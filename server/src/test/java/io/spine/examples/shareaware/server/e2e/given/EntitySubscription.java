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

import io.spine.base.CommandMessage;
import io.spine.base.EntityState;
import io.spine.client.Client;
import io.spine.core.UserId;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Subscription for the {@code EntityState} changes.
 */
public class EntitySubscription<S extends EntityState> {

    private final ObservedEntity<S> entity = new ObservedEntity<>();

    private final Client client;

    private final UserId user;

    EntitySubscription(Class<S> entityType, Client client, UserId user) {
        this.client = client;
        this.user = user;
        client.onBehalfOf(user)
              .subscribeTo(entityType)
              .observe(entity::setState)
              .post();
    }

    /**
     * Provides the current state of the subscribed entity
     * if it arrived by the time when this method is called, null otherwise.
     */
    @Nullable
    public S state() {
        S state = entity.state();
        return state;
    }

    /**
     * Posts the command on behalf of the user and waits for an update of the entity state that
     * should happen as a consequence of the posted command.
     *
     * <p>If an update of the entity state is not received within 10 seconds,
     * a {@code TimeoutException} is thrown.
     */
    S onceUpdatedAfter(CommandMessage command) {
        entity.clearState();
        client.onBehalfOf(user)
                .command(command)
                .postAndForget();
        return entity.waitForUpdate();
    }

    private static final class ObservedEntity<S extends EntityState> {

        private CompletableFuture<S> future = new CompletableFuture<>();

        private void setState(S value) {
            if (future.isDone()) {
                future = new CompletableFuture<>();
            }
            future.complete(value);
        }

        /**
         * Returns the current state of the entity if it exists, null otherwise.
         */
        @Nullable
        private S state() {
            try {
                if (future.isDone()) {
                    return future.get();
                }
                return null;
            } catch (InterruptedException | ExecutionException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        /**
         * Waits for an update of the entity state to arrive and return this state.
         *
         * <p>An update of the entity state should be received within 10 seconds,
         * otherwise a {@code TimeoutException} will be thrown.
         */
        private S waitForUpdate() {
            try {
                return future.whenComplete((value, error) -> {
                                 if (error != null) {
                                     throw illegalStateWithCauseOf(error);
                                 }
                             })
                             .orTimeout(10, SECONDS)
                             .get();
            } catch (InterruptedException | ExecutionException e) {
                throw illegalStateWithCauseOf(e);
            }
        }

        /**
         * Clears the state of the entity.
         */
        private void clearState() {
            future = new CompletableFuture<>();
        }
    }
}
