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

package io.spine.examples.shareaware.server;

import io.spine.environment.Tests;
import io.spine.server.ServerEnvironment;
import io.spine.server.delivery.Delivery;
import io.spine.testing.server.blackbox.ContextAwareTest;
import io.spine.testing.server.model.ModelTests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * An abstract base for {@link io.spine.testing.server.blackbox.BlackBoxContext
 * BlackBoxContext}-based tests, which may assemble the bounded-context-under-tests
 * with different entities, and therefore require to clear the cached knowledge
 * about {@linkplain io.spine.server.model.Model Bounded Context models}.
 *
 * @implNote This implementation {@linkplain ModelTests#dropAllModels() clears all models}
 * before and after each test. Also, test-scope {@code Delivery} is reset
 * to bound the delivery operations to the scope of their respective test method.
 */
public abstract class FreshContextTest extends ContextAwareTest {

    @Override
    @BeforeEach
    protected void createContext() {
        ModelTests.dropAllModels();
        resetDelivery();
        super.createContext();
    }

    /**
     * Explicitly configures a new local {@code Delivery} to use.
     *
     * <p>This is a required step, since many test actions happen asynchronously,
     * leading to a number of signals to be delivered out of scope
     * of their designated test. If the {@code Delivery} is not reset,
     * signals emitted by tests may remain in the delivery pipeline,
     * causing various side effects for the tests which follow.
     */
    private static void resetDelivery() {
        ServerEnvironment.when(Tests.class).use(Delivery.local());
    }

    @Override
    @AfterEach
    protected void closeContext() {
        super.closeContext();
        ModelTests.dropAllModels();
    }
}
