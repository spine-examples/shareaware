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

package io.spine.examples.shareaware.server.wallet;

import io.spine.examples.shareaware.server.given.GivenMoney;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.google.common.truth.Truth.*;

@DisplayName("`MoneyCalculator` should")
final class MoneyCalculatorTest extends UtilityClassTest<MoneyCalculator> {

    MoneyCalculatorTest() {
        super(MoneyCalculator.class);
    }

    @ParameterizedTest
    @DisplayName("calculate sum of two `Money` objects")
    @CsvSource({
            "20, 50, 20, 50, 41, 0",
            "50, 0, 25, 50, 75, 50",
            "100, 90, 50, 20, 151, 10"
    })
    void calculateMoney(
            int firstUnits, int firstNanos,
            int secondUnits, int secondNanos,
            int expectedUnits, int expectedNanos) {
        Money first = GivenMoney.generatedWith(firstUnits, firstNanos, Currency.USD);
        Money second = GivenMoney.generatedWith(secondUnits, secondNanos, Currency.USD);
        Money expected = GivenMoney.generatedWith(expectedUnits, expectedNanos, Currency.USD);
        Money actual = MoneyCalculator.sum(first, second);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("throw an `IllegalStateException` when `Money` objects have different currencies")
    void differentCurrencies() {
        Money first = GivenMoney.generatedWith(20, Currency.USD);
        Money second = GivenMoney.generatedWith(30, Currency.UAH);

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class,
                                        () -> MoneyCalculator.sum(first, second));
        assertThat(exception.getMessage()).
                isEqualTo("Cannot calculate two `Money` objects with different currencies.");
    }

    @ParameterizedTest
    @DisplayName("throw an `IllegalStateException` when `Money` units are negative")
    @CsvSource({
            "20, -50",
            "-10, 10"
    })
    void negativeUnits(int firstUnits, int secondUnits) {
        Money first = GivenMoney.generatedWith(firstUnits, Currency.USD);
        Money second = GivenMoney.generatedWith(secondUnits, Currency.USD);

        IllegalStateException exception =
                Assertions.assertThrows(IllegalStateException.class,
                                        () -> MoneyCalculator.sum(first, second));
    }

    @ParameterizedTest
    @DisplayName("throw an `IllegalArgumentException` when `Money` units out of 0..100 range")
    @CsvSource({
            "120, 50",
            "10, -10"
    })
    void outOfBounds(int firstNanos, int secondNanos) {
        Money first = GivenMoney.generatedWith(0, firstNanos, Currency.USD);
        Money second = GivenMoney.generatedWith(0, secondNanos, Currency.USD);

        IllegalArgumentException exception =
                Assertions.assertThrows(IllegalArgumentException.class,
                                        () -> MoneyCalculator.sum(first, second));
    }
}
