package io.spine.examples.shareaware.server.watchlist;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.core.UserId;
import io.spine.examples.shareaware.watchlist.UserWatchlists;
import io.spine.examples.shareaware.watchlist.event.WatchlistCreated;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRoute;
import io.spine.server.route.EventRouting;

/**
 * Manages instances of user watchlists projections.
 */
public class UserWatchlistsRepository
        extends ProjectionRepository<UserId, UserWatchlistsProjection, UserWatchlists> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<UserId> routing) {
        super.setupEventRouting(routing);

        routing.route(WatchlistCreated.class,
                      (event, context) -> EventRoute.withId(event.getUser()));
    }
}
