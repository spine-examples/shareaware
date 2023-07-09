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

package io.spine.examples.shareaware.server.market

import com.google.protobuf.Duration
import com.google.protobuf.Timestamp

public fun Timestamp.minus(timestamp: Timestamp): Duration {
    val duration = Duration.newBuilder()

    duration.seconds = this.seconds - timestamp.seconds
    duration.nanos = this.nanos - timestamp.nanos

    if (duration.seconds < 0 && duration.nanos > 0) {
        duration.seconds += 1
        duration.nanos -= 1000000000
    } else if (duration.seconds > 0 && duration.nanos < 0) {
        duration.seconds -= 1
        duration.nanos += 1000000000
    }
    return duration.build()
}

public fun Duration.greaterThen(duration: Duration): Boolean {
    if (this.seconds > duration.seconds) {
        return true
    }
    if (this.seconds <= duration.seconds) {
        return false
    }
    return this.nanos > duration.nanos
}
