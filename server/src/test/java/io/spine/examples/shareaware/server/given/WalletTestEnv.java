package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.BlackBoxContext;

import static io.spine.examples.shareaware.server.given.GivenMoney.moneyOf;

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

    /**
     * Generates {@code ReplenishWallet} command.
     */
    public static ReplenishWallet replenish(WalletId wallet, Money amount) {
        Iban iban = Iban
                .newBuilder()
                .setValue("FI211234569876543210")
                .vBuild();
        ReplenishmentId replenishment = ReplenishmentId.generate();
        return ReplenishWallet
                .newBuilder()
                .setWallet(wallet)
                .setReplenishment(replenishment)
                .setIban(iban)
                .setMoneyAmount(amount)
                .vBuild();
    }

    /**
     * Generates {@code ReplenishWallet} command on 500 USD for the wallet.
     */
    public static ReplenishWallet replenish(WalletId wallet) {
        Money replenishmentAmount = moneyOf(500, Currency.USD);
        return replenish(wallet, replenishmentAmount);
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
