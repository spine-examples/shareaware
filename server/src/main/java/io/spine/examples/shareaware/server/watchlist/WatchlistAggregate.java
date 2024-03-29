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

package io.spine.examples.shareaware.server.watchlist;

import io.spine.examples.shareaware.WatchlistId;
import io.spine.examples.shareaware.watchlist.Watchlist;
import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
import io.spine.examples.shareaware.watchlist.command.WatchShare;
import io.spine.examples.shareaware.watchlist.event.ShareWatched;
import io.spine.examples.shareaware.watchlist.event.WatchlistCreated;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.Apply;
import io.spine.server.command.Assign;

/**
 * A list of shares watched by a particular ShareAware user.
 */
public final class WatchlistAggregate extends Aggregate<WatchlistId, Watchlist, Watchlist.Builder> {

    /**
     * Handles the command to create a watchlist.
     */
    @Assign
    WatchlistCreated handle(CreateWatchlist c) {
        return WatchlistCreated
                .newBuilder()
                .setOwner(c.getUser())
                .setWatchlist(c.getWatchlist())
                .setName(c.getName())
                .vBuild();
    }

    @Apply
    private void event(WatchlistCreated e) {
        builder().setId(e.getWatchlist())
                 .setOwner(e.getOwner())
                 .setName(e.getName());
    }

    @Assign
    ShareWatched handle(WatchShare c) {
        return ShareWatched
                .newBuilder()
                .setWatchlist(c.getWatchlist())
                .setUser(c.getUser())
                .setShare(c.getShare())
                .vBuild();
    }

    @Apply
    private void event(ShareWatched e) {
        builder().addShare(e.getShare());
    }
}
