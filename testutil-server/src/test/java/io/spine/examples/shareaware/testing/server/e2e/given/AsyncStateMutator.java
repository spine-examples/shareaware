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

package io.spine.examples.shareaware.testing.server.e2e.given;

import io.spine.util.Exceptions;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * State mutator that working in a separate thread.
 */
public class AsyncStateMutator {

    private final Consumer<Consumer<String>> mutationNotifier;

    private final AtomicBoolean isMutated = new AtomicBoolean(false);

    private final AtomicReference<String> state = new AtomicReference<>();

    private final ExecutorService thread = newSingleThreadExecutor();

    private final Random random = new SecureRandom();

    private final Object lock = new Object();

    private volatile boolean keepRunning = true;

    public AsyncStateMutator(String state, Duration delay) {
        this.state.set(state);
        mutationNotifier = consumer ->
                thread.execute(() -> {
                    while (keepRunning) {
                        synchronized (lock) {
                            while (!isMutated.get() && keepRunning) {
                                try {
                                    lock.wait(10000);
                                } catch (InterruptedException e) {
                                    throw Exceptions.illegalStateWithCauseOf(e);
                                }
                            }
                        }
                        if (isMutated.get()) {
                            sleepUninterruptibly(delay);
                            consumer.accept(this.state.get());
                            isMutated.set(false);
                        }
                    }
                });
    }

    /**
     * Mutate the state by adding the random number to it.
     */
    public synchronized void mutateState(Boolean isMutating) {
        if (isMutating) {
            int additionalState = random.nextInt(100);
            state.set(state.get() + additionalState);
        }
        isMutated.set(isMutating);
        if (isMutating) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }

    /**
     * Stops the thread where the {@code AsyncStateMutator} is executing.
     */
    public void stopMutatorThread() {
        keepRunning = false;
        synchronized (lock) {
            lock.notifyAll();
        }
        thread.shutdown();
    }

    /**
     * Returns the current state.
     */
    public String state() {
        return state.get();
    }

    /**
     * Returns the {@code Consumer} that notifies the caller when the state has been mutated.
     */
    public Consumer<Consumer<String>> mutationNotifier() {
        return mutationNotifier;
    }
}
