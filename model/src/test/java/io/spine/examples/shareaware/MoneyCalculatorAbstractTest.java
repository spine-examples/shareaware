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

package io.spine.examples.shareaware;

import com.google.common.testing.NullPointerTester;
import io.spine.money.Currency;
import io.spine.money.Money;
import io.spine.testing.UtilityClassTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.BiFunction;

import static com.google.common.truth.Truth.assertThat;
import static io.spine.examples.shareaware.given.GivenMoney.moneyOf;
import static io.spine.examples.shareaware.given.GivenMoney.usd;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The abstract base designed for {@link MoneyCalculator} testing.
 *
 * <p>Inheritors must fill parametrized tests for most common {@link MoneyCalculator} operations with arguments.
 * Also, there are generic methods for testing math and boolean operations
 * in case {@code MoneyCalculator} will extend.
 */
abstract class MoneyCalculatorAbstractTest extends UtilityClassTest<MoneyCalculator> {

    MoneyCalculatorAbstractTest() {
        super(MoneyCalculator.class);
    }

    @Override
    protected void configure(NullPointerTester tester) {
        super.configure(tester);
        tester.setDefault(Money.class, usd(0));
    }

    @ParameterizedTest
    @MethodSource("sum")
    void testSum(Money first, Money second, Money expected) {
        testMathCalculation(first, second, expected, MoneyCalculator::sum);
    }

    @ParameterizedTest
    @MethodSource("subtract")
    void testSubtract(Money first, Money second, Money expected) {
        testMathCalculation(first, second, expected, MoneyCalculator::subtract);
    }

    @ParameterizedTest
    @MethodSource("isGreater")
    void testIsGreater(Money first, Money second, boolean expected) {
        testBooleanCalculation(first, second, expected, MoneyCalculator::isGreater);
    }

    @ParameterizedTest
    @MethodSource("multiply")
    void testMultiply(Money money, int multiplier, Money expected) {
        testMathCalculation(money, multiplier, expected, MoneyCalculator::multiply);
    }

    <R> void testValidation(BiFunction<Money, Money, R> operation) {
        testDifferentCurrencies(operation);
        testNegativeUnits(operation);
        testNanosOutOfBounds(operation);
    }

    private static void testMathCalculation(Money first, Money second, Money expected,
                                            BiFunction<Money, Money, Money> operation) {
        var actual = operation.apply(first, second);

        assertThat(actual).isEqualTo(expected);
    }

    private static void testMathCalculation(Money money, int multiplier, Money expected,
                                            BiFunction<Money, Integer, Money> operation) {
        var actual = operation.apply(money, multiplier);

        assertThat(actual).isEqualTo(expected);
    }

    private static void testBooleanCalculation(Money first, Money second, boolean expected,
                                               BiFunction<Money, Money, Boolean> operation) {
        boolean actual = operation.apply(first, second);

        assertThat(actual).isEqualTo(expected);
    }

    private static <R> void testDifferentCurrencies(BiFunction<Money, Money, R> operation) {
        var first = moneyOf(20, Currency.UAH);
        var second = moneyOf(30, Currency.USD);

        var exception = assertThrows(IllegalStateException.class,
                                     () -> operation.apply(first, second));
        assertThat(exception.getMessage())
                .isEqualTo("Cannot calculate two `Money` objects with different currencies.");
    }

    private static <R> void testNegativeUnits(BiFunction<Money, Money, R> operation) {
        var positiveUnits = usd(20);
        var negativeUnits = usd(-50);

        assertThrows(IllegalStateException.class,
                     () -> operation.apply(positiveUnits, negativeUnits));
        assertThrows(IllegalStateException.class,
                     () -> operation.apply(negativeUnits, positiveUnits));
    }

    private static <R> void testNanosOutOfBounds(BiFunction<Money, Money, R> operation) {
        var positiveUnits = usd(0, 120);
        var negativeUnits = usd(0, -10);

        assertThrows(IllegalArgumentException.class,
                     () -> operation.apply(positiveUnits, negativeUnits));
        assertThrows(IllegalArgumentException.class,
                     () -> operation.apply(negativeUnits, positiveUnits));
    }
}
