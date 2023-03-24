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

    public static Money moneyOf(long units, Currency currency) {
        return Money
                .newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNanos(0)
                .vBuild();
    }

    public static Money moneyOf(long units, int nanos, Currency currency) {
        return Money
                .newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNanos(nanos)
                .vBuild();
    }

    public static Money usd(long units, int nanos) {
        return moneyOf(units, nanos, Currency.USD);
    }

    public static Money usd(long units) {
        return moneyOf(units, Currency.USD);
    }
}
