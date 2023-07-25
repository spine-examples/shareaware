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

import io.spine.examples.shareaware.testing.server.e2e.given.AsyncStateMutator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static com.google.common.truth.Truth.assertThat;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("`AsyncObserver` should")
class AsyncObserverTest {

    @Test
    @DisplayName("observe mutation of the state with the slow response from the mutator")
    void observeMutationWithDelay() {
        var stateMutator = new AsyncStateMutator("state", ofSeconds(5));
        var observer = new AsyncObserver<>(stateMutator.mutationNotifier(),
                                           stateMutator::mutateState);

        var actualStateAfterFirstMutation = observer.onceUpdatedAfter(true);
        var stateAfterFirstMutation = stateMutator.state();
        assertThat(actualStateAfterFirstMutation).isEqualTo(stateAfterFirstMutation);
        assertThat(observer.state()).isEqualTo(stateAfterFirstMutation);

        var actualStateAfterSecondMutation = observer.onceUpdatedAfter(true);
        var stateAfterSecondMutation = stateMutator.state();
        assertThat(actualStateAfterSecondMutation).isEqualTo(stateAfterSecondMutation);
        assertThat(observer.state()).isEqualTo(stateAfterSecondMutation);

        stateMutator.stopMutatorThread();
    }

    @Test
    @DisplayName("observe mutation of the state with the fast response from the mutator")
    void observeMutationWithoutDelay() {
        var stateMutator = new AsyncStateMutator("state", ofSeconds(0));
        var observer = new AsyncObserver<>(stateMutator.mutationNotifier(),
                                           stateMutator::mutateState);

        var actualStateAfterFirstMutation = observer.onceUpdatedAfter(true);
        var stateAfterFirstMutation = stateMutator.state();
        assertThat(actualStateAfterFirstMutation).isEqualTo(stateAfterFirstMutation);
        assertThat(observer.state()).isEqualTo(stateAfterFirstMutation);

        stateMutator.stopMutatorThread();
    }

    @Test
    @DisplayName("throw the `IllegalArgumentException` with  cause of `TimeoutException`" +
            "when the state mutation were not received in 10 seconds")
    void throwExceptionWithoutMutation() {
        var stateMutator = new AsyncStateMutator("", ofSeconds(0));
        var observer = new AsyncObserver<>(stateMutator.mutationNotifier(),
                                           stateMutator::mutateState);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                             () -> observer.onceUpdatedAfter(false));
        assertThat(exception.getCause()
                            .getClass()).isEqualTo(TimeoutException.class);

        stateMutator.stopMutatorThread();
    }
}
