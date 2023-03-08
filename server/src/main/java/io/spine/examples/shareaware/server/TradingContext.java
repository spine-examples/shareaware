package io.spine.examples.shareaware.server;

import io.spine.examples.shareaware.server.watchlist.WatchlistAggregate;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.DefaultRepository;

/**
 * Configures Trading Bounded Context with repositories.
 */
public final class TradingContext {

    static final String NAME = "Trading";

    /**
     * Prevents instantiation of this utility class.
     */
    private TradingContext() {

    }

    /**
     * Creates {@code BoundedContextBuilder} for the Trading context and fills it with
     * repositories.
     */
    public static BoundedContextBuilder newBuilder() {
        return BoundedContext
                .singleTenant(NAME)
                .add(DefaultRepository.of(WatchlistAggregate.class));
    }
}
