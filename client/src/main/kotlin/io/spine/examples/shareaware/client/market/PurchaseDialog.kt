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

package io.spine.examples.shareaware.client.market

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.MoneyCalculator
import io.spine.examples.shareaware.client.component.NumericInput
import io.spine.examples.shareaware.client.component.Dialog
import io.spine.examples.shareaware.client.asReadableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * Displays the dialog window for the shares purchase purposes.
 *
 * @param purchaseModel the model of the shares purchase operation
 */
@Composable
public fun PurchaseDialog(
    purchaseModel: PurchaseOperationModel
) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    val isShown by purchaseModel.isInProgress().collectAsState()
    val shareToPurchase by purchaseModel.share().collectAsState()
    val quantity by purchaseModel.quantityOfShares().collectAsState()
    if (isShown) {
        Dialog(
            onCancel = {
                scope.launch {
                    purchaseModel.cancel()
                }
            },
            onConfirm = {
                scope.launch {
                    purchaseModel.complete()
                    purchaseModel.cancel()
                }
            },
            title = "Purchase '${shareToPurchase?.companyName}' shares",
            modifier = Modifier
                .wrapContentHeight()
                .width(245.dp),
            {
                val price = MoneyCalculator
                    .multiply(shareToPurchase!!.price, quantity)
                    .asReadableString()
                PurchaseDialogInput(
                    price = price,
                    onChange = { purchaseModel.quantityOfShares(it) }
                )
            }
        )
    }
}

/**
 * Displays the input inside the `PurchaseDialog`.
 *
 * @param price total price of the purchase to be displayed
 * @param onChange callback that will be triggered when the value of the input changes
 */
@Composable
private fun PurchaseDialogInput(
    price: String,
    onChange: (Int) -> Unit
) {
    Column {
        Text(
            "Total Price - $price",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp, bottom = 15.dp)
        )
        NumericInput(
            placeholder = "How much to purchase",
            onChange = {
                val quantity = if (it == "") 0 else it.toInt()
                onChange(quantity)
            }
        )
    }
}
