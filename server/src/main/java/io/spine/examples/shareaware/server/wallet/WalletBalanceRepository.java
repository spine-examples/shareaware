package io.spine.examples.shareaware.server.wallet;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import io.spine.core.UserId;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.route.EventRoute;
import io.spine.server.route.EventRouting;

/**
 * Manages instances of {@code Wallet} projections.
 */
public final class WalletBalanceRepository
        extends ProjectionRepository<UserId, WalletBalanceProjection, WalletBalance> {

    @OverridingMethodsMustInvokeSuper
    @Override
    protected void setupEventRouting(EventRouting<UserId> routing) {
        super.setupEventRouting(routing);
        routing.route(WalletCreated.class,
                      (event, context) -> EventRoute.withId(event.getWallet()));
    }
}
