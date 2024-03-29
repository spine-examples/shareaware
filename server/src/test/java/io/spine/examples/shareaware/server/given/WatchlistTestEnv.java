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

import io.spine.core.UserId;
import io.spine.examples.shareaware.ShareId;
import io.spine.examples.shareaware.WatchlistId;
import io.spine.examples.shareaware.watchlist.Watchlist;
import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
import io.spine.examples.shareaware.watchlist.command.WatchShare;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.testing.TestValues.randomString;

public final class WatchlistTestEnv {

    /**
     * Prevents instantiation of this class.
     */
    private WatchlistTestEnv() {
    }

    public static CreateWatchlist createWatchlist() {
        return CreateWatchlist
                .newBuilder()
                .setUser(GivenUserId.generated())
                .setWatchlist(WatchlistId.generate())
                .setName(randomString())
                .vBuild();
    }

    public static Watchlist setUpWatchlist(BlackBoxContext context) {
        var command = createWatchlist();
        context.receivesCommand(command);
        return Watchlist
                .newBuilder()
                .setId(command.getWatchlist())
                .setOwner(command.getUser())
                .setName(command.getName())
                .vBuild();
    }

    public static WatchShare watchShare(WatchlistId watchlist, UserId owner) {
        return WatchShare
                .newBuilder()
                .setWatchlist(watchlist)
                .setUser(owner)
                .setShare(ShareId.generate())
                .vBuild();
    }
}
