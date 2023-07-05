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

package io.spine.examples.shareaware.client

import io.spine.money.Currency
import io.spine.money.Money

/**
 * Returns the readable `String` constructed from the `Money` object.
 */
public fun Money.asReadableString(): String {
    return "$" + this.units.toString() + "." + this.nanos.toString()
}

/**
 * Returns a new `Money` object in USD currency using this `String` to construct it.
 *
 * This `String` must be written as a number with a decimal point.
 */
public fun String.asUsd(): Money {
    val parts = this.split('.')
    val units = parts[0].toLong()
    val nanos = if (parts.size == 2) parts[1].toInt() else 0
    return Money
        .newBuilder()
        .setCurrency(Currency.USD)
        .setUnits(units)
        .setNanos(nanos)
        .vBuild()
}

/**
 * Returns true if this `String` is written like a number with a decimal point,
 * and it can be converted to a `Money` object, false otherwise.
 */
public fun String.validateMoney(): Boolean {
    val decimalRegex = """^\d+(\.\d{1,2})?${'$'}""".toRegex()
    return !decimalRegex.containsMatchIn(this)
}
