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
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.watchlist.Watchlist;
import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
import io.spine.examples.shareaware.watchlist.event.WatchlistCreated;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.EventSubject;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static io.spine.testing.TestValues.randomString;

@DisplayName("`Watchlist` should")
public final class WatchlistTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingContext.newBuilder();
    }

    /**
     * Verifies that a command to create a watchlist generates corresponding event.
     */
    @Nested
    @DisplayName("allow creation")
    class Creation {

        @Test
        @DisplayName("as entity with the `Watchlist` state")
        void entity() {
            CreateWatchlist command = generateCommand();

            context().receivesCommand(command);

            Watchlist expected = Watchlist
                    .newBuilder()
                    .setId(command.getWatchlist())
                    .setName(command.getName())
                    .vBuild();

            context().assertState(command.getWatchlist(), expected);
        }

        @Test
        @DisplayName("emitting the `WatchlistCreated` event")
        void event() {
            CreateWatchlist command = generateCommand();

            context().receivesCommand(command);

            WatchlistCreated expected = WatchlistCreated
                    .newBuilder()
                    .setOwner(command.getUser())
                    .setWatchlist(command.getWatchlist())
                    .setName(command.getName())
                    .vBuild();

            EventSubject assertEvents = context()
                    .assertEvents()
                    .withType(WatchlistCreated.class);

            assertEvents.hasSize(1);

            context().assertEvent(expected);
        }

        private CreateWatchlist generateCommand() {
            return CreateWatchlist
                    .newBuilder()
                    .setUser(GivenUserId.newUuid())
                    .setWatchlist(WatchlistId.generate())
                    .setName(randomString())
                    .vBuild();
        }
    }
}
