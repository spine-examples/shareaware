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

import io.spine.base.KnownMessage;
import io.spine.client.Subscription;

import java.util.concurrent.CompletableFuture;

/**
 * The outcome of the subscription that was created by the {@link E2EUser}.
 */
public final class SubscriptionOutcome<S extends KnownMessage> {

    private final CompletableFuture<S> future;

    private final Subscription subscription;

    SubscriptionOutcome(CompletableFuture<S> future, Subscription subscription) {
        this.future = future;
        this.subscription = subscription;
    }

    /**
     * Returns the future which stores the received message of the subscribed type.
     *
     * <p>The received message should be retrieved from future in this way:
     * <pre> {@code
     *     CompletableFuture future = subscriptionOutcome.future();
     *     future.get(timeout, timeunit);
     * }
     * </pre>
     *
     * <p>We recommend using {@code future.get()} with timeout
     * because the simple {@code future.get()} method will block the calling
     * thread until the future is completed, but there is no guarantee
     * that the future will be completed at all.
     */
    public CompletableFuture<S> future() {
        return future;
    }

    /**
     * Returns the subscription object.
     *
     * <p>In case when the subscription is no longer needed,
     * it is recommended to cancel it via {@code Client}.
     */
    Subscription subscription() {
        return subscription;
    }
}
