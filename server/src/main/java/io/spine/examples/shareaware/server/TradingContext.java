package io.spine.examples.shareaware.server;

import io.spine.examples.shareaware.server.watchlist.WatchlistAggregate;
import io.spine.server.BoundedContext;
import io.spine.server.BoundedContextBuilder;
import io.spine.server.DefaultRepository;

public final class TradingContext {

    static final String NAME = "Trading";

    private TradingContext() {

    }

    public static BoundedContextBuilder newBuilder() {
        return BoundedContext
                .singleTenant(NAME)
                .add(DefaultRepository.of(WatchlistAggregate.class));
    }
}
