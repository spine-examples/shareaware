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

package io.spine.examples.shareaware.market;

import com.google.errorprone.annotations.Immutable;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import io.spine.annotation.GeneratedMixin;
import io.spine.base.EntityState;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.SharePriceMovementId;

/**
 * Common interface for projections that display the movements of the share price.
 */
@Immutable
@GeneratedMixin
public interface SharePriceMovement extends EntityState {

    /**
     * Returns the ID of the {@code SharePriceMovement} projection.
     */
    SharePriceMovementId getId();

    /**
     * Returns the time when the projection was created.
     */
    default Timestamp whenCreated() {
        return getId().getWhenCreated();
    }

    /**
     * Returns the activity period of the projection.
     *
     * <p>The period when it is collecting data about the share price movements.
     */
    default Duration activityTime() {
        return getId().getActivityTime();
    }

    /**
     * Returns the ID of the share which price movements the projection displays.
     */
    default ShareId shareFromId() {
        return getId().getShare();
    }
}
