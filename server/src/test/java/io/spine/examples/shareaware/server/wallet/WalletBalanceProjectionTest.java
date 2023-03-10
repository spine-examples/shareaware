package io.spine.examples.shareaware.server.wallet;

import io.spine.examples.shareaware.server.TradingContext;
import io.spine.examples.shareaware.wallet.WalletBalance;
import io.spine.examples.shareaware.wallet.command.CreateWallet;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.server.BoundedContextBuilder;
import io.spine.testing.core.given.GivenUserId;
import io.spine.testing.server.blackbox.ContextAwareTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("`WalletBalance` should")
public final class WalletBalanceProjectionTest extends ContextAwareTest {

    @Override
    protected BoundedContextBuilder contextBuilder() {
        return TradingContext.newBuilder();
    }

    @Test
    @DisplayName("display an empty balance, as soon as the wallet created")
    void balance() {
        CreateWallet command  = CreateWallet
                .newBuilder()
                .setWallet(GivenUserId.generated())
                .vBuild();
        Money expectedBalance = Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(0)
                .setNanos(0)
                .vBuild();
        WalletBalance expected = WalletBalance
                .newBuilder()
                .setId(command.getWallet())
                .setBalance(expectedBalance)
                .vBuild();
        context().receivesCommand(command);

        context().assertState(command.getWallet(), expected);
    }
}
