package io.spine.examples.shareaware.server.given;

import io.spine.money.Currency;
import io.spine.money.Money;

public final class GivenMoney {

    /**
     * Prevents instantiation of this utility class.
     */
    private GivenMoney() {
    }

    public static Money zero() {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(0)
                .setNanos(0)
                .vBuild();
    }

    public static Money generatedWith(long units, Currency currency) {
        return Money
                .newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNanos(0)
                .vBuild();
    }
}
