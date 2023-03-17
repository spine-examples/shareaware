package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.replenishmentcommand.ReplenishWallet;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

public final class WalletTestEnv {

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

    public static CreateWallet createWallet(WalletId id) {
        return CreateWallet
                .newBuilder()
                .setWallet(id)
                .vBuild();
    }

    public static ReplenishWallet
    replenish(WalletId wallet, ReplenishmentId replenishment, Money amount) {
        Iban iban = Iban
                .newBuilder()
                .setValue("FI211234569876543210")
                .vBuild();
        return ReplenishWallet
                .newBuilder()
                .setWallet(wallet)
                .setReplenishment(replenishment)
                .setIban(iban)
                .setMoneyAmount(amount)
                .vBuild();
    }

    /**
     * Creates a {@code Wallet} in {@code context} by sending {@code CreateWallet} command to it.
     * @return the ID of created {@code Wallet}.
     */
    public static WalletId setupWallet(BlackBoxContext context) {
        WalletId wallet = givenId();
        CreateWallet command = createWallet(wallet);
        context.receivesCommand(command);
        return wallet;
    }
}
