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

package io.spine.examples.shareaware.server.investment;

import io.spine.core.Subscribe;
import io.spine.core.UserId;
import io.spine.examples.shareaware.InvestmentId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.investment.HeldShares;
import io.spine.examples.shareaware.investment.event.SharesPurchased;
import io.spine.examples.shareaware.investment.event.SharesSold;
import io.spine.server.projection.Projection;

/**
 * View of held shares by the user per {@code Investment}.
 */
public final class HeldSharesProjection
        extends Projection<InvestmentId, HeldShares, HeldShares.Builder> {

    @Subscribe
    void on(SharesPurchased e) {
        UserId owner = e.getPurchaser();
        ShareId share = e.getShare();
        builder()
                .setId(investmentId(owner, share))
                .setSharesAvailable(e.getSharesAvailable());
    }

    @Subscribe
    void on(SharesSold e) {
        UserId owner = e.getSeller();
        ShareId share = e.getShare();
        builder()
                .setId(investmentId(owner, share))
                .setSharesAvailable(e.getSharesAvailable());
    }

    private static InvestmentId investmentId(UserId owner, ShareId share) {
        return InvestmentId
                .newBuilder()
                .setOwner(owner)
                .setShare(share)
                .vBuild();
    }
}
