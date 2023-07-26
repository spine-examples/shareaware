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

package io.spine.examples.shareaware.testing.server.e2e;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Observer for entity state changes.
 *
 * <p>Allows to observe the asynchronous mutations of the entity state.
 *
 * @param <S>
 *         the state to observe
 * @param <C>
 *         the type of the command, the execution of which should lead to changes in the state
 */
public class AsyncObserver<S, C> {

    private final Consumer<C> howToCommand;

    private CompletableFuture<S> statePack = new CompletableFuture<>();

    private @Nullable S state = null;

    private final AtomicReference<ObservationState> observationState =
            new AtomicReference<>(null);

    /**
     * Creates the new instance of the {@code AsyncObserver}.
     *
     * @param howToObserve
     *         a callback that defines how to observe the state {@link S}.
     * @param howToCommand
     *         a callback that defines how to send a command {@link C}.
     */
    public AsyncObserver(
            StateRouter<S> howToObserve,
            Consumer<C> howToCommand) {
        this.howToCommand = howToCommand;
        howToObserve.route(value -> {
            state = value;
            if (statePack.isDone()) {
                statePack = new CompletableFuture<>();
            }
            statePack.complete(value);
            observationState.set(ObservationState.UPDATE_PACKED);
        });
    }

    /**
     * Posts a command and waits for an update of the entity state that
     * should occur as a consequence of the posted command.
     *
     * <p>If an update of the entity state is not received within 10 seconds,
     * a {@code TimeoutException} is thrown.
     */
    public S onceUpdatedAfter(C command) {
        howToCommand.accept(command);
        return waitForUpdate();
    }

    /**
     * Returns the current state of the entity if it exists, null otherwise.
     */
    public @Nullable S state() {
        return state;
    }

    /**
     * Waits for an update of the entity state to arrive and return this state.
     *
     * <p>An update of the entity state should be received within 10 seconds,
     * otherwise a {@code TimeoutException} will be thrown.
     */
    private S waitForUpdate() {
        checkForUpdate();
        S updatedEntity = unpackState();
        observationState.set(ObservationState.UPDATE_UNPACKED);
        return updatedEntity;
    }

    /**
     * Checks for the update of the entity state to arrive
     * at the moment when this method is called.
     *
     * <p>If the update of the entity state has not arrived
     * this method forces the waiting for update.
     */
    private void checkForUpdate() {
        if (observationState.get() != null &&
                observationState.get() != ObservationState.UPDATE_PACKED) {
            statePack = new CompletableFuture<>();
        }
    }

    /**
     * Unpack the entity state from the {@link #statePack}.
     *
     * <p>If the entity state cannot be unpacked within 10 seconds,
     * a {@code TimeoutException} will be thrown.
     */
    private S unpackState() {
        try {
            return statePack.whenComplete((value, error) -> {
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
     * Represents the states of the entity observation.
     *
     * <p>The {@code AsyncObserver} can observe asynchronous changes of the entity's state,
     * and these states were introduced to restrict the order of the observing operations.
     */
    private enum ObservationState {

        // The update entity state has arrived asynchronously
        // and packed for synchronization with the main thread.
        UPDATE_PACKED,

        // The update of the entity state has been unpacked and ready for usage in the main thread.
        UPDATE_UNPACKED
    }
}
