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

import io.spine.money.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static io.spine.examples.shareaware.server.given.GivenMoney.*;
import static org.junit.jupiter.params.provider.Arguments.*;

@DisplayName("`MoneyCalculator` should")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class MoneyCalculatorTest extends MoneyCalculatorAbstractTest {

    @DisplayName("calculate the sum of two `Money` objects")
    Stream<Arguments> sum() {
        return Stream.of(
                of(
                        moneyOf(20, 50, Currency.USD),
                        moneyOf(20, 50, Currency.USD),
                        moneyOf(41, Currency.USD)),
                of(
                        moneyOf(50, Currency.USD),
                        moneyOf(25, 50, Currency.USD),
                        moneyOf(75, 50, Currency.USD)),
                of(
                        moneyOf(100, 90, Currency.USD),
                        moneyOf(50, 20, Currency.USD),
                        moneyOf(151, 10, Currency.USD))
        );
    }

    @DisplayName("calculate the difference of two `Money` objects")
    Stream<Arguments> subtract() {
        return Stream.of(
                of(
                        moneyOf(20, 50, Currency.USD),
                        moneyOf(20, 50, Currency.USD),
                        moneyOf(0, Currency.USD)),
                of(
                        moneyOf(26, 10, Currency.USD),
                        moneyOf(25, 50, Currency.USD),
                        moneyOf(0, 60, Currency.USD)),
                of(
                        moneyOf(100, 50, Currency.USD),
                        moneyOf(90, 50, Currency.USD),
                        moneyOf(10, Currency.USD))
        );
    }

    @DisplayName("determine if the `Money` object is greater then the second")
    Stream<Arguments> isGreater() {
        return Stream.of(
                of(
                        moneyOf(20, 50, Currency.USD),
                        moneyOf(20, 50, Currency.USD),
                        false),
                of(
                        moneyOf(26, 10, Currency.USD),
                        moneyOf(25, 50, Currency.USD),
                        true),
                of(
                        moneyOf(40, 50, Currency.USD),
                        moneyOf(90, 50, Currency.USD),
                        false)
        );
    }
}
