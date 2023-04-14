/*
 * Copyright 2023, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.shareaware.given;

import io.spine.money.Currency;
import io.spine.money.Money;

/**
 * Factory methods for creating {@code Money} instances for test purposes.
 */
public final class GivenMoney {

    /**
     * Prevents instantiation of this utility class.
     */
    private GivenMoney() {
    }

    /**
     * Returns the {@code Money} instance with zero value in the USD currency.
     */
    public static Money zero() {
        return Money
                .newBuilder()
                .setCurrency(Currency.USD)
                .setUnits(0)
                .setNanos(0)
                .vBuild();
    }

    /**
     * Creates the instance of {@code Money} with provided values of
     * {@link Money#units_} and {@link Money#currency_}.
     *
     * <p>The {@link Money#nanos_} value is set to zero by default.
     */
    public static Money moneyOf(long units, Currency currency) {
        return Money
                .newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNanos(0)
                .vBuild();
    }

    /**
     * Creates the instance of {@code Money} with provided values of {@link Money#units_},
     * {@link Money#nanos_}, and {@link Money#currency_}.
     */
    public static Money moneyOf(long units, int nanos, Currency currency) {
        return Money
                .newBuilder()
                .setCurrency(currency)
                .setUnits(units)
                .setNanos(nanos)
                .vBuild();
    }

    /**
     * Creates the {@code Money} instance in USD currency with provided values of
     * {@link Money#units_} and {@link Money#nanos_}.
     */
    public static Money usd(long units, int nanos) {
        return moneyOf(units, nanos, Currency.USD);
    }

    /**
     * Creates the {@code Money} instance in USD currency
     * with a provided value of {@link Money#units_}.
     *
     * <p>The {@link Money#nanos_} value is set to zero by default.
     */
    public static Money usd(long units) {
        return moneyOf(units, Currency.USD);
    }
}
