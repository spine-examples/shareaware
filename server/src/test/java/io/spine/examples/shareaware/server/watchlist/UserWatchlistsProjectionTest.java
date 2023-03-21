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

import io.spine.core.UserId;
import io.spine.examples.shareaware.WatchlistId;
import io.spine.examples.shareaware.server.TradingTestContext;
import io.spine.examples.shareaware.watchlist.UserWatchlists;
import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.watchlist.UserWatchlists.*;

@DisplayName("`UserWatchlistsProjection` should")
public final class UserWatchlistsProjectionTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingTestContext.newBuilder();
    }

    @Test
    @DisplayName("display all user's watchlists, as soon as they are created")
    void watchlists() {
        UserId user = GivenUserId.generated();
        CreateWatchlist firstCommand = generateCommand(user, "first");
        CreateWatchlist secondCommand = generateCommand(user, "second");
        WatchlistView firstWatchlist = generateWatchlistView(firstCommand);
        WatchlistView secondWatchlist = generateWatchlistView(secondCommand);
        UserWatchlists expected = UserWatchlists
                .newBuilder()
                .setId(firstCommand.getUser())
                .addWatchlist(firstWatchlist)
                .addWatchlist(secondWatchlist)
                .vBuild();
        context().receivesCommands(firstCommand, secondCommand);

        context().assertState(user, expected);
    }

    private static CreateWatchlist generateCommand(UserId user, String watchlistName) {
        return CreateWatchlist
                .newBuilder()
                .setUser(user)
                .setWatchlist(WatchlistId.generate())
                .setName(watchlistName)
                .vBuild();
    }

    private static WatchlistView generateWatchlistView(CreateWatchlist command) {
        return WatchlistView
                .newBuilder()
                .setId(command.getWatchlist())
                .setName(command.getName())
                .vBuild();
    }
}
