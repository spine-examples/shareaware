//package io.spine.examples.shareaware.server.watchlist;
//
//import com.google.common.truth.extensions.proto.ProtoSubject;
//import io.spine.examples.shareaware.WatchlistId;
//import io.spine.examples.shareaware.server.TradingContext;
//import io.spine.examples.shareaware.watchlist.UserWatchlists;
//import io.spine.examples.shareaware.watchlist.Watchlist;
//import io.spine.examples.shareaware.watchlist.command.CreateWatchlist;
//import io.spine.server.BoundedContextBuilder;
//import io.spine.testing.server.blackbox.ContextAwareTest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static io.spine.examples.shareaware.watchlist.UserWatchlists.*;
//import static io.spine.testing.TestValues.randomString;
//
//public class UserWatchlistsProjectionTest extends ContextAwareTest {
//
//    private CreateWatchlist command;
//    private CreateWatchlist second;
//
//    private ProtoSubject entityState;
//
//    @Override
//    protected BoundedContextBuilder contextBuilder() {
//        return TradingContext.newBuilder();
//    }
//
//    @BeforeEach
//    void setupWatchlists() {
//        WatchlistId watchlistId = WatchlistId.generate();
//
//        command = CreateWatchlist
//                .newBuilder()
//                .setWatchlist(watchlistId)
//                .setName("1")
//                .vBuild();
//
//        WatchlistId secondId = WatchlistId.generate();
//
//        second = CreateWatchlist
//                .newBuilder()
//                .setWatchlist(secondId)
//                .setName("2")
//                .vBuild();
//
//        context().receivesCommands(second, command);
//
//        entityState = context()
//                .assertEntity(secondId, UserWatchlistsProjection.class)
//                .hasStateThat();
//    }
//
//    @Test
//    void watchlists() {
//
//        UserWatchlists expected = UserWatchlists.newBuilder()
//                .addWatchlist(WatchlistView.newBuilder()
//                                           .setId(second.getWatchlist())
//                                           .setName(second.getName())
//                                           .vBuild())
//                .vBuild();
//
////        context().assertState(command.getWatchlist(), expected);
//
//        entityState.isEqualTo(expected);
//    }
//}
