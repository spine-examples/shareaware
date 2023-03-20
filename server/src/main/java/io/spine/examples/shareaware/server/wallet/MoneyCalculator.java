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

import static com.google.common.base.Preconditions.*;
import static io.spine.util.Preconditions2.*;

/**
 * The calculator for {@code spine.Money}.
 *
 * <p>Please note, this implementation works properly only with currencies containing 100 coins in one unit.
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
     * Calculates the sum of two {@code Money} objects with the same currency.
     */
    static Money sum(Money first, Money second) {
        checkNotNull(first);
        checkNotNull(second);
        checkState(first.getCurrency() == second.getCurrency(),
                                 "Cannot calculate two `Money` objects with different currencies.");
        checkState(first.getUnits() >= 0 && second.getUnits() >= 0);
        checkBounds(first.getNanos(), "first.nanos", 0, MAX_NANOS_AMOUNT);
        checkBounds(second.getNanos(), "second.nanos", 0, MAX_NANOS_AMOUNT);

        int resultNanos = first.getNanos() + second.getNanos();
        long resultUnits = first.getUnits() + second.getUnits();
        if (resultNanos / NANOS_IN_UNIT >= 1) {
            resultUnits++;
            resultNanos -= NANOS_IN_UNIT;
        }
        return Money
                .newBuilder()
                .setNanos(resultNanos)
                .setUnits(resultUnits)
                .setCurrency(first.getCurrency())
                .vBuild();
    }
}
