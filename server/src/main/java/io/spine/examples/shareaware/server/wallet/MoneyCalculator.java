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

import com.google.common.base.Preconditions;
import io.spine.money.Money;
import io.spine.util.Preconditions2;

/**
 * The calculator for `spine.Money`.
 * <p>
 * Note: work properly only with currencies that contain 100 coins in one unit.
 */
final class MoneyCalculator {

    private static final int MAX_NANOS_AMOUNT = 99;
    private static final int NANOS_IN_UNIT = 100;

    /**
     * Prevents instantiations of this class.
     */
    private MoneyCalculator() {
    }

    /**
     * Calculates the sum of two Money objects with the same currency.
     */
    static Money summarize(Money firstTerm, Money secondTerm) {
        Preconditions.checkState(firstTerm.getCurrency() == secondTerm.getCurrency(),
                                 "Cannot calculate two `Money` objects with different currencies.");
        Preconditions.checkState(firstTerm.getUnits() >= 0 && secondTerm.getUnits() >= 0);
        Preconditions2.checkBounds(firstTerm.getNanos(), "firstTerm.nanos", 0, MAX_NANOS_AMOUNT);
        Preconditions2.checkBounds(secondTerm.getNanos(), "secondTerm.nanos", 0, MAX_NANOS_AMOUNT);

        int summarizedNanos = firstTerm.getNanos() + secondTerm.getNanos();
        long summarizedUnits = firstTerm.getUnits() + secondTerm.getUnits();
        if (summarizedNanos / NANOS_IN_UNIT >= 1) {
            summarizedUnits++;
            summarizedNanos -= NANOS_IN_UNIT;
        }
        return Money
                .newBuilder()
                .setNanos(summarizedNanos)
                .setUnits(summarizedUnits)
                .setCurrency(firstTerm.getCurrency())
                .vBuild();
    }
}
