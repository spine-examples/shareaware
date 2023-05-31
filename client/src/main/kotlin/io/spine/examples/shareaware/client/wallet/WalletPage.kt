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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.spine.client.EventFilter.*
import io.spine.examples.shareaware.ReplenishmentId
import io.spine.examples.shareaware.client.DesktopClient
import io.spine.examples.shareaware.client.EntitySubscription
import io.spine.examples.shareaware.client.Icons
import io.spine.examples.shareaware.client.PrimaryButton
import io.spine.examples.shareaware.client.payment.Dialog
import io.spine.examples.shareaware.client.payment.WarningTooltip
import io.spine.examples.shareaware.paymentgateway.rejection.Rejections.MoneyCannotBeTransferredFromUser
import io.spine.examples.shareaware.wallet.Iban
import io.spine.examples.shareaware.wallet.WalletBalance
import io.spine.examples.shareaware.wallet.command.ReplenishWallet
import io.spine.examples.shareaware.wallet.event.WalletReplenished
import io.spine.money.Currency
import io.spine.money.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * UI model for the `WalletPage`.
 */
public class WalletPageModel(private val client: DesktopClient) {
    private var replenishmentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var withdrawalState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val balanceSubscription: EntitySubscription<WalletBalance> =
        EntitySubscription(WalletBalance::class.java, client, client.wallet())
    private var replenishmentError: MutableStateFlow<Boolean> =
        MutableStateFlow(false)

    /**
     * Sets page to default state.
     */
    public fun toDefaultState() {
        replenishmentState.value = false
        withdrawalState.value = false
    }

    /**
     * Sets page to "replenishment" state.
     *
     * Page state when the user wants to replenish the wallet.
     */
    public fun toReplenishmentState() {
        replenishmentState.value = true
        withdrawalState.value = false
    }

    /**
     * Sets page to "withdrawal" state.
     *
     * Page state when the user wants to withdraw money from the wallet.
     */
    public fun toWithdrawalState() {
        withdrawalState.value = true
        replenishmentState.value = false
    }

    /**
     * Returns the "replenishment" state of the page.
     */
    public fun replenishmentState(): StateFlow<Boolean> {
        return replenishmentState
    }

    /**
     * Returns the "withdrawal" state of the page.
     */
    public fun withdrawalState(): StateFlow<Boolean> {
        return withdrawalState
    }

    /**
     * Returns the current state of the wallet balance.
     */
    public fun balance(): StateFlow<WalletBalance?> {
        return balanceSubscription.state()
    }

    /**
     * Returns the current state of the payment error during the replenishment.
     */
    public fun replenishmentError(): StateFlow<Boolean> {
        return replenishmentError
    }

    /**
     * Cancels the state of the payment error during the replenishment.
     */
    public fun cancelReplenishmentError() {
        replenishmentError.value = false
    }

    /**
     * Sends the `ReplenishWallet` command to the server.
     *
     * @param ibanValue the IBAN of the user
     * @param moneyAmount the amount of money to replenish the wallet
     */
    public fun replenishWallet(ibanValue: String, moneyAmount: String) {
        val replenishWallet = ReplenishWallet
            .newBuilder()
            .buildWith(ibanValue, moneyAmount)
        subscribeToWalletReplenished(replenishWallet.replenishment)
        subscribeToReplenishmentError(replenishWallet.replenishment)
        client.command(replenishWallet)
    }

    /**
     * Subscribes to the `WalletReplenished` event that signals
     * about the successful ending of the wallet replenishment process.
     *
     * @param id the ID of the replenishment process
     */
    private fun subscribeToWalletReplenished(id: ReplenishmentId) {
        val replenishmentIdField = WalletReplenished.Field.replenishment()
        client.subscribeOnce(
            WalletReplenished::class.java,
            eq(replenishmentIdField, id)
        ) {
            replenishmentError.value = false
        }
    }

    /**
     * Subscribes to the `MoneyCannotBeTransferredFromUser` event that signals about
     * failure in the payment system, during the wallet replenishment process.
     *
     * @param id the ID of the replenishment process
     */
    private fun subscribeToReplenishmentError(id: ReplenishmentId) {
        val replenishmentIdField = MoneyCannotBeTransferredFromUser.Field.replenishment()
        client.subscribeOnce(
            MoneyCannotBeTransferredFromUser::class.java,
            eq(replenishmentIdField, id)
        ) {
            replenishmentError.value = true
        }
    }

    /**
     * Returns the command to replenish the wallet.
     *
     * @param ibanValue the IBAN of the user
     * @param amount the amount of money to replenish the wallet
     */
    private fun ReplenishWallet.Builder.buildWith(ibanValue: String, amount: String): ReplenishWallet {
        return this
            .setReplenishment(ReplenishmentId.generate())
            .setWallet(client.wallet())
            .setIban(ibanValue.asIban())
            .setMoneyAmount(amount.asMoney())
            .vBuild()
    }

    /**
     * Returns a new `Money` object using this `String` to construct it.
     *
     * This `String` must be written as a number with decimal point.
     */
    private fun String.asMoney(): Money {
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
     * Returns a new IBAN using this `String` as its value.
     *
     * [Rules for making up the IBAN](https://en.wikipedia.org/wiki/International_Bank_Account_Number#:~:text=of%20total%20payments-,Structure,-%5Bedit%5D)
     */
    private fun String.asIban(): Iban {
        return Iban
            .newBuilder()
            .setValue(this)
            .vBuild()
    }
}

/**
 * The page component that provides data about
 * the user's current wallet balance and ways to interact with it.
 */
@Composable
public fun WalletPage(model: WalletPageModel): Unit = Column {
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
                val balance by model.balance().collectAsState()
                Text(
                    "Balance: $${balance?.asReadableString()}",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                )
            }
        }
        val scope = rememberCoroutineScope { Dispatchers.Default }
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
                PrimaryButton({
                    scope.launch {
                        model.toReplenishmentState()
                    }
                }, "Replenish")
            }
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                PrimaryButton({
                    scope.launch {
                        model.toWithdrawalState()
                    }
                }, "Withdraw")
            }
        }
        val replenishmentState = model
            .replenishmentState()
            .collectAsState()
        val replenishmentError = model
            .replenishmentError()
            .collectAsState()
        var replenishmentIbanValue by remember { mutableStateOf("") }
        var replenishmentAmount by remember { mutableStateOf("") }
        MoneyOperationDialog(
            onCancel = { model.toDefaultState() },
            onConfirm = {
                scope.launch {
                    model.replenishWallet(replenishmentIbanValue, replenishmentAmount)
                }
            },
            isShown = replenishmentState.value,
            title = "Wallet Replenishment",
            ibanValue = replenishmentIbanValue,
            onIbanChange = { replenishmentIbanValue = it },
            moneyValue = replenishmentAmount,
            onMoneyChange = { replenishmentAmount = it }
        )
        val withdrawalState = model
            .withdrawalState()
            .collectAsState()
        var withdrawalIbanValue by remember { mutableStateOf("") }
        var withdrawalAmount by remember { mutableStateOf("") }
        MoneyOperationDialog(
            onCancel = { model.toDefaultState() },
            onConfirm = {},
            isShown = withdrawalState.value,
            title = "Wallet Withdrawal",
            ibanValue = withdrawalIbanValue,
            onIbanChange = { withdrawalIbanValue = it },
            moneyValue = withdrawalAmount,
            onMoneyChange = { withdrawalAmount = it }
        )
        Row(
            Modifier
                .height(40.dp)
                .padding(start = 1.dp)
        ) {
            PopUpMessage(
                isShown = replenishmentError.value,
                dismissAction = {
                    scope.launch {
                        model.cancelReplenishmentError()
                    }
                },
                label = "An error occurred while the wallet was replenished."
            )
        }
    }
}

/**
 * Pop-up message component.
 *
 * @param isShown is a component shown to the user
 * @param dismissAction callback that will be triggered when the user clicks on `Cancel` button
 * @param label the message to be shown to the user
 */
@Composable
private fun PopUpMessage(
    isShown: Boolean,
    dismissAction: () -> Unit,
    label: String
) {
    if (isShown) {
        Snackbar(
            modifier = Modifier
                .widthIn(200.dp, 550.dp)
                .wrapContentWidth(),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.error,
            dismissAction = {
                Row (
                    modifier = Modifier
                        .padding(end = 10.dp)
                ) {
                    PrimaryButton(
                        onClick = dismissAction,
                        label = "Cancel",
                        modifier = Modifier
                            .width(110.dp)
                            .height(30.dp),
                        labelStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        ) { Text(label) }
    }
}

/**
 * Returns the readable `String` constructed from the `WalletBalance` projection.
 */
private fun WalletBalance.asReadableString(): String {
    return this.balance.units.toString() + "." + this.balance.nanos.toString()
}

/**
 * Dialog window component with a form for money operations.
 *
 * @param isShown is a dialog window shown to the user
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
    isShown: Boolean,
    title: String,
    ibanValue: String,
    onIbanChange: (String) -> Unit,
    moneyValue: String,
    onMoneyChange: (String) -> Unit
) {
    if (isShown) {
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
                    label = "IBAN",
                    icon = painterResource(Icons.CARD),
                    iconDescription = "IBAN",
                    isError = mistakeInIbanField,
                    errorMessage = "Ensure your IBAN " +
                            "contains 2 letters and 2 digits in the beginning and " +
                            "up to 26 alphanumeric characters after. " +
                            "Example: FI211234569876543210"
                )
            },
            {
                Input(
                    value = moneyValue,
                    onValueChange = onMoneyChange,
                    label = "How much",
                    icon = painterResource(Icons.USD),
                    iconDescription = "USD Currency",
                    isError = mistakeInMoneyField,
                    errorMessage = "This field must contain only digits. Example: 500.50"
                )
            }
        )
    }
}


/**
 * The input component that supports displaying an error related to its state.
 *
 * @param value the input text to be shown in the text field
 * @param onValueChange the callback that is triggered when the input's value change
 * @param label the label to be displayed inside the input container
 * @param icon leading icon to be displayed at the beginning of the input container
 * @param iconDescription what is icon represents
 * @param isError indicates if the input's current value is in error
 * @param errorMessage error message to be displayed in the tooltip when [isError] set to `true`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun Input(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: Painter,
    iconDescription: String,
    isError: Boolean,
    errorMessage: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = MaterialTheme.colorScheme.surface,
            textColor = MaterialTheme.colorScheme.onSurface,
            placeholderColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.onSurface
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        isError = isError,
        leadingIcon = {
            Icon(
                painter = icon,
                contentDescription = iconDescription,
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )
        },
        trailingIcon = {
            if (isError) {
                WarningTooltip(errorMessage)
            }
        }
    )
}

private fun String.validateIban(): Boolean {
    val ibanRegex =
        """[A-Z]{2}[0-9]{2}(?:[ ]?[0-9]{4}){4}(?!(?:[ ]?[0-9]){3})(?:[ ]?[0-9]{1,2})?""".toRegex()
    return !ibanRegex.containsMatchIn(this)
}

private fun String.validateMoney(): Boolean {
    val decimalRegex = """^\d+(\.\d{1,2})?${'$'}""".toRegex()
    return !decimalRegex.containsMatchIn(this)
}
