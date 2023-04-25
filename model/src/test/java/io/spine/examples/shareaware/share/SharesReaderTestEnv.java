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

package io.spine.examples.shareaware.share;

import com.google.common.collect.ImmutableSet;
import io.spine.examples.shareaware.ShareId;

import java.util.Set;

import static io.spine.examples.shareaware.given.GivenMoney.*;

final class SharesReaderTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private SharesReaderTestEnv() {
    }

    static Set<Share> expectedSharesFromFile() {
        Share goodShare = Share
                .newBuilder()
                .setId(ShareId.of("9c6456b3-eccb-48db-90d3-af2595f77f59"))
                .setPrice(usd(100, 50))
                .setCompanyName("AwesomeCompany")
                .setCompanyLogo("https://awesome.site.org/images/logo.svg")
                .vBuild();
        Share awesomeShare = Share
                .newBuilder()
                .setId(ShareId.of("4b8326b3-eccb-48db-45d3-af2595d55f59"))
                .setPrice(usd(100, 50))
                .setCompanyName("GoodCompany")
                .setCompanyLogo("https://good.site.org/images/logo.svg")
                .vBuild();
        return ImmutableSet.of(goodShare, awesomeShare);
    }
}
