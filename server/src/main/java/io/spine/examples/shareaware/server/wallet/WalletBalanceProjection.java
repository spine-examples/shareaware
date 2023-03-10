package io.spine.examples.shareaware.server.wallet;

import io.spine.core.Subscribe;
import io.spine.core.UserId;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.event.WalletCreated;
import io.spine.server.projection.Projection;

public class WalletBalanceProjection
        extends Projection<UserId, WalletBalance, WalletBalance.Builder> {

    @Subscribe
    void on(WalletCreated e) {
        builder()
                .setId(e.getWallet());
    }
}
