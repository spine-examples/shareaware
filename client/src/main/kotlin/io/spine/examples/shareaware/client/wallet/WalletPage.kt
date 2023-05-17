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

package io.spine.examples.shareaware.client.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.client.payment.Dialog
import io.spine.examples.shareaware.client.payment.Input
import io.spine.examples.shareaware.client.PrimaryButton

/**
 * The page component that provides data about
 * the user's current wallet balance and ways to interact with it.
 */
@Composable
public fun WalletPage(): Unit = Column {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ElevatedCard (
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .width(350.dp)
                    .height(100.dp)
                    .padding(vertical = 15.dp, horizontal = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 20.dp,
                ),
            ) {
                Text(
                    "Balance: 200$",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )
            }
        }
        var replenishmentState by remember { mutableStateOf(false) }
        var withdrawalState by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                PrimaryButton({ replenishmentState = true }, "Replenish")
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                PrimaryButton({ withdrawalState = true }, "Withdraw")
            }
        }
        var replenishmentIbanValue by remember { mutableStateOf("") }
        var replenishmentAmount by remember { mutableStateOf("") }
        if (replenishmentState) {
            MoneyOperationDialog(
                onCancel = { replenishmentState = false },
                onConfirm = {},
                title = "Wallet Replenishment",
                ibanValue = replenishmentIbanValue,
                onIbanChange = { replenishmentIbanValue = it },
                moneyValue = replenishmentAmount,
                onMoneyChange = { replenishmentAmount = it }
            )
        }
        var withdrawalIbanValue by remember { mutableStateOf("") }
        var withdrawalAmount by remember { mutableStateOf("") }
        if (withdrawalState) {
            MoneyOperationDialog(
                onCancel = { withdrawalState = false },
                onConfirm = {},
                title = "Wallet Withdrawal",
                ibanValue = withdrawalIbanValue,
                onIbanChange = { withdrawalIbanValue = it },
                moneyValue = withdrawalAmount,
                onMoneyChange = { withdrawalAmount = it }
            )
        }
    }
}

/**
 * Dialog window component with a from for money operations.
 *
 * @param ibanValue the IBAN value to be shown in the relevant input
 * @param onIbanChange the callback that is triggered when the IBAN value change
 * @param moneyValue the money amount value to be shown in the relevant input
 * @param onMoneyChange the callback that is triggered when the money amount value change
 *
 * @see Dialog
 */
@Composable
private fun MoneyOperationDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    ibanValue: String,
    onIbanChange: (String) -> Unit,
    moneyValue: String,
    onMoneyChange: (String) -> Unit
) {
    var mistakeInIbanField by remember { mutableStateOf(false) }
    var mistakeInMoneyField by remember { mutableStateOf(false) }
    Dialog(
        onCancel = onCancel,
        onConfirm = {
            mistakeInIbanField = ibanValue.validateIban()
            mistakeInMoneyField = moneyValue.validateMoney()
            if (!mistakeInIbanField && !mistakeInMoneyField) {
                onConfirm()
                onCancel()
            }
        },
        title = title,
        {
            Input(
                value = ibanValue,
                onValueChange = onIbanChange,
                label = "Please enter your IBAN",
                isError = mistakeInIbanField,
                errorMessage = "Please ensure that your IBAN " +
                        "contains 2 letters and 2 digits in the beginning and " +
                        "up to 26 alphanumeric characters after." +
                        "Example: FI211234569876543210"
            )
        },
        {
            Input(
                value = moneyValue,
                onValueChange = onMoneyChange,
                label = "Please enter money amount",
                isError = mistakeInMoneyField,
                errorMessage = "Please enter only digits. Example: 500.50"
            )
        }
    )
}

private fun String.validateIban(): Boolean {
    val ibanRegex =
        """[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?!(?:[ ]?[0-9]){3})(?:[ ]?[0-9]{1,2})?""".toRegex()
    return !ibanRegex.containsMatchIn(this)
}

private fun String.validateMoney(): Boolean {
    val decimalRegex = """^\d+\.?\d*${'$'}""".toRegex()
    return !decimalRegex.containsMatchIn(this)
}
