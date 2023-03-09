package io.spine.examples.shareaware.server.watchlist;

import com.google.common.truth.extensions.proto.ProtoSubject;
import io.spine.core.UserId;
import io.spine.examples.shareaware.WatchlistId;
import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.watchlist.UserWatchlists;
import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.shareaware.watchlist.UserWatchlists.*;
import static io.spine.testing.TestValues.randomString;

@DisplayName("`UserWatchlistsProjection` should")
public class UserWatchlistsProjectionTest extends ContextAwareTest {

    private CreateWatchlist firstCommand;

    private CreateWatchlist secondCommand;

    private ProtoSubject entityState;

    private UserId user;

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingContext.newBuilder();
    }

    @BeforeEach
    void setupWatchlists() {

        user = UserId.newBuilder()
                            .setValue(randomString())
                            .vBuild();

        firstCommand = CreateWatchlist
                .newBuilder()
                .setUser(user)
                .setWatchlist(WatchlistId.generate())
                .setName("1")
                .vBuild();

        secondCommand = CreateWatchlist
                .newBuilder()
                .setUser(user)
                .setWatchlist(WatchlistId.generate())
                .setName("2")
                .vBuild();

        context().receivesCommands(firstCommand, secondCommand);

        entityState = context()
                .assertEntity(user, UserWatchlistsProjection.class)
                .hasStateThat();
    }

    @Test
    @DisplayName("have the state with the ID of the user")
    void entity() {
        UserWatchlists expected = UserWatchlists
                .newBuilder()
                .setId(user)
                .build();

        entityState.isInstanceOf(UserWatchlists.class);
        entityState.comparingExpectedFieldsOnly()
                   .isEqualTo(expected);
    }

    @Test
    @DisplayName("have watchlists")
    void watchlists() {

        UserWatchlists expected = UserWatchlists
                .newBuilder()
                .setId(firstCommand.getUser())
                .addWatchlist(WatchlistView.newBuilder()
                                           .setId(firstCommand.getWatchlist())
                                           .setName(firstCommand.getName())
                                           .vBuild())
                .addWatchlist(WatchlistView.newBuilder()
                                           .setId(secondCommand.getWatchlist())
                                           .setName(secondCommand.getName())
                                           .vBuild())
                .vBuild();

        entityState.isEqualTo(expected);
    }
}
