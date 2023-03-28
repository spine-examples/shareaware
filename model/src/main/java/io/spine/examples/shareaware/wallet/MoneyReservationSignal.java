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

package io.spine.examples.shareaware.wallet;

import com.google.errorprone.annotations.Immutable;
import io.spine.annotation.GeneratedMixin;
import io.spine.base.EventMessage;
import io.spine.examples.shareaware.OperationId;
import io.spine.examples.shareaware.WithdrawalId;

/**
 * Common interface for signals participating in the money reservation operation.
 */
@Immutable
@GeneratedMixin
public interface MoneyReservationSignal extends EventMessage {

    /**
     * Returns the ID of operation, in the scope of which this signal is emitted.
     */
    OperationId getOperation();

    /**
     * Retrieves {@code WithdrawalId} from {@code OperationId} without checking for its existence.
     *
     * <p>In case when {@code WithdrawalId} is not set in the corresponding {@code OperationId}, returns the
     * <a href="https://protobuf.dev/reference/java/java-generated/#:~:text=static%20Foo%20getDefaultInstance,its%20newBuilderForType()%20method.">
     * default instance</a> of {@code WithdrawalId}.
     */
    default WithdrawalId getWithdrawalProcess() {
        return getOperation().getWithdrawal();
    }

    /**
     * Retrieves the value of {@code WithdrawalId}
     * without checking for {@code WithdrawalId} existence in {@code OperationId}.
     *
     * <p>In case when {@code WithdrawalId} is not set in the corresponding {@code OperationId}, returns an empty string.
     */
    default String getWithdrawalIdValue() {
        return getOperation().getWithdrawal().getUuid();
    }

    /**
     * Verifies that signal is a part of the withdrawal process or not.
     */
    default boolean isPartOfWithdrawal() {
        return getOperation().hasWithdrawal();
    }
}
