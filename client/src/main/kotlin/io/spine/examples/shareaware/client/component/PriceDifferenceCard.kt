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

package io.spine.examples.shareaware.client.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.MoneyCalculator
import io.spine.examples.shareaware.client.MoneyExtensions.asReadableString
import io.spine.money.Money

/**
 * Displays the card that shows difference between two `Money` objects.
 */
@Composable
public fun PriceDifferenceCard(actualPrice: Money, previousPrice: Money?) {
    val color: Color
    val price: String
    if (null == previousPrice) {
        color = MaterialTheme.colorScheme.surfaceVariant
        price = "~0.0"
    } else if (MoneyCalculator.isGreater(actualPrice, previousPrice)) {
        color = MaterialTheme.colorScheme.surfaceVariant
        price = "+${MoneyCalculator.subtract(actualPrice, previousPrice).asReadableString()}"
    } else {
        color = MaterialTheme.colorScheme.error
        price = "-${MoneyCalculator.subtract(previousPrice, actualPrice).asReadableString()}"
    }
    Box(
        modifier = Modifier
            .background(color, MaterialTheme.shapes.extraSmall)
            .padding(2.dp),
    ) {
        Text(
            text = price,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
