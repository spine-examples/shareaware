package io.spine.examples.shareaware.server.watchlist;

import io.spine.core.Subscribe;
import io.spine.core.UserId;
import io.spine.examples.shareaware.WatchlistId;
import io.spine.examples.shareaware.watchlist.UserWatchlists;
import io.spine.examples.shareaware.watchlist.event.WatchlistCreated;
import io.spine.server.projection.Projection;

import static io.spine.examples.shareaware.watchlist.UserWatchlists.*;

/**
 * Builds display information for all user's watchlists.
 */
public final class UserWatchlistsProjection
        extends Projection<UserId, UserWatchlists, UserWatchlists.Builder> {

    @Subscribe
    void on(WatchlistCreated e) {

        WatchlistView watchlistView = WatchlistView.newBuilder()
                                                   .setId(e.getWatchlist())
                                                   .setName(e.getName())
                                                   .vBuild();

        builder().addWatchlist(watchlistView);
    }
}
