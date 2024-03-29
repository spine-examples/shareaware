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

package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.share.Share;
import io.spine.examples.shareaware.ShareId;

import static io.spine.examples.shareaware.given.GivenMoney.*;

/**
 * Provides an API to create test instances of the shares.
 */
public final class GivenShare {

    private static final ShareId teslaId = ShareId.generate();
    private static final ShareId appleId = ShareId.generate();

    /**
     * Prevents instantiation of this class.
     */
    private GivenShare() {
    }

    public static Share tesla() {
        return Share
                .newBuilder()
                .setId(teslaId)
                .setPrice(usd(20))
                .setCompanyName("Tesla")
                .setCompanyLogo("testURL")
                .vBuild();
    }

    public static Share apple() {
        return Share
                .newBuilder()
                .setId(appleId)
                .setPrice(usd(20))
                .setCompanyName("Apple")
                .setCompanyLogo("testURL")
                .vBuild();
    }
}
