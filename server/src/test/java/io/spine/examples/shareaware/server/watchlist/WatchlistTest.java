package io.spine.examples.shareaware.server.watchlist;

import io.spine.examples.shareaware.WatchlistId;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.watchlist.Watchlist;
import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
import io.spine.examples.shareaware.watchlist.event.WatchlistCreated;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.EventSubject;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static io.spine.testing.TestValues.randomString;

@DisplayName("`Watchlist` should")
class WatchlistTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingContext.newBuilder();
    }

    /**
     * Verifies that a command to create a watchlist generates corresponding event.
     */
    @Nested
    @DisplayName("create a watchlist")
    class Creation {

        private CreateWatchlist command;

        @BeforeEach
        void setupWatchlist() {

            command = CreateWatchlist
                    .newBuilder()
                    .setWatchlist(WatchlistId.generate())
                    .setName(randomString())
                    .vBuild();

            context().receivesCommand(command);
        }

        @Test
        @DisplayName("as entity with the `Watchlist` state")
        void entity() {

            Watchlist expected = Watchlist.newBuilder()
                                          .setId(command.getWatchlist())
                                          .setName(command.getName())
                                          .vBuild();

            context().assertState(command.getWatchlist(), expected);
        }

        @Test
        @DisplayName("emitting the `WatchlistCreated` event")
        void event() {

            WatchlistCreated expected = WatchlistCreated
                    .newBuilder()
                    .setWatchlist(command.getWatchlist())
                    .setName(command.getName())
                    .vBuild();

            EventSubject assertEvents = context()
                    .assertEvents()
                    .withType(WatchlistCreated.class);

            assertEvents.hasSize(1);

            context().assertEvent(expected);
        }
    }
}
