package io.spine.examples.shareaware.server.given;

import io.spine.examples.shareaware.OperationId;
import io.spine.examples.shareaware.ReplenishmentId;
import io.spine.examples.shareaware.WalletId;
import io.spine.examples.shareaware.WithdrawalId;
import io.spine.examples.shareaware.wallet.Iban;
import io.spine.examples.shareaware.wallet.Wallet;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.examples.shareaware.wallet.command.ReplenishWallet;
import io.spine.examples.shareaware.wallet.command.WithdrawMoney;
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

    private static final Iban IBAN = Iban
            .newBuilder()
            .setValue("FI211234569876543210")
            .vBuild();

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
        ReplenishmentId replenishment = ReplenishmentId.generate();
        return ReplenishWallet
                .newBuilder()
                .setWallet(wallet)
                .setReplenishment(replenishment)
                .setIban(IBAN)
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
     *
     * @return the ID of created {@code Wallet}.
     */
    public static WalletId setUpWallet(BlackBoxContext context) {
        WalletId wallet = givenId();
        CreateWallet command = createWallet(wallet);
        context.receivesCommand(command);
        return wallet;
    }

    public static Wallet setUpReplenishedWallet(BlackBoxContext context) {
        WalletId wallet = setUpWallet(context);
        ReplenishWallet command = replenish(wallet);
        context.receivesCommand(command);
        return Wallet
                .newBuilder()
                .setId(wallet)
                .setBalance(command.getMoneyAmount())
                .vBuild();
    }

    public static WithdrawMoney withdraw(WalletId wallet) {
        return WithdrawMoney
                .newBuilder()
                .setWithdrawalProcess(WithdrawalId.generate())
                .setWallet(wallet)
                .setRecipient(IBAN)
                .setAmount(moneyOf(200, Currency.USD))
                .vBuild();
    }

    public static OperationId operationId(WithdrawalId withdrawal) {
        return OperationId
                .newBuilder()
                .setWithdrawal(withdrawal)
                .vBuild();
    }
}
