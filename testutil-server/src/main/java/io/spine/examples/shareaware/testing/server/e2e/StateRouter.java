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
 * Represents a state router to deliver the state to the {@code StateRecipient}.
 *
 * <p>A state router acts as a bridge between the subject that produces the state
 * and the recipients that are interested in receiving and processing this state.
 *
 * @param <S> the type of the state to be routed to the {@code StateRecipient}
 */
public interface StateRouter<S> extends Consumer<StateRecipient<S>> {

    /**
     * Routes the state to the specified recipient.
     *
     * @param recipient the recipient to which the state should be routed.
     */
    void route(StateRecipient<S> recipient);

    /**
     * Accepts a {@code StateRecipient} and routes the state to it.
     *
     * @param recipient the recipient to which the state should be routed.
     * @implNote Inheritors can choose to override this method or directly implement
     * the {@link #route} method to route the state.
     */
    @Override
    default void accept(StateRecipient<S> recipient) {
        route(recipient);
    }
}
