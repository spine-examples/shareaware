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

import java.util.function.Consumer;

/**
 * Represents a recipient of state updates.
 *
 * <p>This interface is designed to provide a standardized way of accepting
 * and handling state updates from various routers or observers.
 *
 * @param <S> the type of the state to be received
 */
public interface StateRecipient<S> extends Consumer<S> {

    /**
     * Receives the updated state.
     *
     * <p>This method is used to receive state updates
     * and define how the recipient should handle it.
     *
     * @param state the updated state to be received and processed
     */
    void receive(S state);

    /**
     * {@inheritDoc}
     *
     * @implNote Inheritors can choose to override this method or directly implement
     * the {@link #receive} method to handle the received state.
     *
     * @param state the updated state to be received and processed
     */
    @Override
    default void accept(S state) {
        receive(state);
    };
}
