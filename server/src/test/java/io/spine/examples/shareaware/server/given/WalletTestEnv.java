package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;

public class WalletTestEnv {

    /**
     * Prevents instantiation of this test environment.
     */
    private WalletTestEnv() {
    }

    public static WalletId givenId() {
        return WalletId
                .newBuilder()
                .setOwner(GivenUserId.generated())
                .vBuild();
    }

    public static CreateWallet command(WalletId id) {
        return CreateWallet
                .newBuilder()
                .setWallet(id)
                .vBuild();
    }

    public static Money zeroMoneyValue() {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(0)
                .setNanos(0)
                .vBuild();
    }
}
