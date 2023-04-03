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

import io.spine.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static io.spine.examples.shareaware.server.given.GivenMoney.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

@DisplayName("`MoneyCalculator` should")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class MoneyCalculatorTest extends MoneyCalculatorAbstractTest {

    @DisplayName("calculate the sum of two `Money` objects")
    Stream<Arguments> sum() {
        return Stream.of(
                arguments(usd(20, 50), usd(20, 50), usd(41, 0)),
                arguments(usd(50), usd(25, 50), usd(75, 50)),
                arguments(usd(100, 90), usd(50, 20), usd(151, 10))
        );
    }

    @DisplayName("calculate the difference of two `Money` objects")
    Stream<Arguments> subtract() {
        return Stream.of(
                arguments(usd(20, 50), usd(20, 50), usd(0)),
                arguments(usd(26, 10), usd(25, 50), usd(0, 60)),
                arguments(usd(100, 50), usd(90, 50), usd(10))
        );
    }

    @DisplayName("determine if the `Money` object is greater then the second")
    Stream<Arguments> isGreater() {
        return Stream.of(
                arguments(usd(20, 50), usd(20, 50), false),
                arguments(usd(26, 10), usd(25, 50), true),
                arguments(usd(40, 50), usd(40, 60), false)
        );
    }

    @DisplayName("multiply the `Money` object on multiplier")
    Stream<Arguments> multiply() {
        return Stream.of(
                arguments(usd(20, 50), 3, usd(61, 50)),
                arguments(usd(26, 10), 10, usd(261)),
                arguments(usd(100, 12), 0, usd(0)),
                arguments(usd(5, 50), 1, usd(5, 50))
        );
    }

    @Test
    @DisplayName("validate arguments when calculating the sum")
    void validateSum() {
        testValidation(MoneyCalculator::sum);
    }

    @Test
    @DisplayName("validate arguments when calculating the difference")
    void validateSubtract() {
        Money smaller = usd(20, 20);
        Money greater = usd(40, 20);

        testValidation(MoneyCalculator::subtract);
        assertThrows(IllegalStateException.class,
                     () -> MoneyCalculator.subtract(smaller, greater));
    }

    @Test
    @DisplayName("validate arguments when determining which of the object are greater")
    void validateIsGreater() {
        testValidation(MoneyCalculator::isGreater);
    }

    @Nested
    @DisplayName("when performing a multiply operation validate arguments")
    class ValidationMultiplyOperation {

        @Test
        @DisplayName("for negative `Money` value")
        void performNegativeUnits() {
            Money negativeUnits = usd(-20);
            assertThrows(IllegalStateException.class,
                         () -> MoneyCalculator.multiply(negativeUnits, 2));
        }

        @Test
        @DisplayName("for out of bounds `Money.nanos` value")
        void performNanosOutOfBounds() {
            Money nanosOutOfBound = usd(0, -120);
            assertThrows(IllegalArgumentException.class,
                         () -> MoneyCalculator.multiply(nanosOutOfBound, 2));
        }

        @Test
        @DisplayName("for negative multiplier value")
        void performNegativeMultiplier() {
            Money money = usd(20);
            assertThrows(IllegalArgumentException.class,
                         () -> MoneyCalculator.multiply(money, -2));
        }
    }
}
