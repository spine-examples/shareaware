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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.spine.client.EventFilter
import io.spine.examples.shareaware.ReplenishmentId
import io.spine.examples.shareaware.WithdrawalId
import io.spine.examples.shareaware.client.DesktopClient
import io.spine.examples.shareaware.client.EntitySubscription
import io.spine.examples.shareaware.client.asIban
import io.spine.examples.shareaware.client.asUsd
import io.spine.examples.shareaware.client.component.ContainerWithPopup
import io.spine.examples.shareaware.client.component.Dialog
import io.spine.examples.shareaware.client.component.Input
import io.spine.examples.shareaware.client.component.PopupConfig
import io.spine.examples.shareaware.client.component.PrimaryButton
import io.spine.examples.shareaware.client.validateIban
import io.spine.examples.shareaware.client.validateMoney
import io.spine.examples.shareaware.paymentgateway.rejection.Rejections.MoneyCannotBeTransferredFromUser
import io.spine.examples.shareaware.paymentgateway.rejection.Rejections.MoneyCannotBeTransferredToUser
import io.spine.examples.shareaware.wallet.WalletBalance
import io.spine.examples.shareaware.wallet.command.ReplenishWallet
import io.spine.examples.shareaware.wallet.command.WithdrawMoney
import io.spine.examples.shareaware.wallet.event.MoneyWithdrawn
import io.spine.examples.shareaware.wallet.event.WalletReplenished
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * The page component that provides data about
 * the user's current wallet balance and ways to interact with it.
 */
@Composable
public fun WalletPage(model: WalletPageModel): Unit = Column {
    val paymentError = model.paymentError().collectAsState()
    val errorMessage = model.paymentErrorMessage().collectAsState()
    ContainerWithPopup(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize(),
        popupConfig = PopupConfig(
            isShown = paymentError.value,
            dismissAction = { model.closePaymentError() },
            label = errorMessage.value,
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .wrapContentWidth()
            ) {
                BalanceCard(model)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReplenishmentButton(model)
                    WithdrawalButton(model)
                }
            }
        }
        WalletReplenishmentWindow(model)
        WalletWithdrawalWindow(model)
    }
}

/**
 * The `Card` component, which contains the wallet balance.
 */
@Composable
private fun BalanceCard(model: WalletPageModel) {
    val balance by model.balance().collectAsState()
    Row {
        Text(
            "Balance: ",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Start,
        )
        Text(
            "$${balance?.asReadableString()}",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * The `Button` component that calls the wallet replenishment process.
 */
@Composable
private fun ReplenishmentButton(model: WalletPageModel) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    PrimaryButton(
        onClick = {
            scope.launch {
                model.toReplenishmentState()
            }
        },
        label = "Replenish"
    )
}

/**
 * The `Button` component that calls the money withdrawal process.
 */
@Composable
private fun WithdrawalButton(model: WalletPageModel) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    PrimaryButton(
        onClick = {
            scope.launch {
                model.toWithdrawalState()
            }
        },
        label = "Withdraw"
    )
}

/**
 * Dialog window for wallet replenishment process.
 */
@Composable
private fun WalletReplenishmentWindow(model: WalletPageModel) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    val replenishmentState = model
        .replenishmentState()
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
}

/**
 * Dialog window for money withdrawal process.
 */
@Composable
private fun WalletWithdrawalWindow(model: WalletPageModel) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    val withdrawalState = model
        .withdrawalState()
        .collectAsState()
    var withdrawalIbanValue by remember { mutableStateOf("") }
    var withdrawalAmount by remember { mutableStateOf("") }
    MoneyOperationDialog(
        onCancel = { model.toDefaultState() },
        onConfirm = {
            scope.launch {
                model.withdrawMoney(withdrawalIbanValue, withdrawalAmount)
            }
        },
        isShown = withdrawalState.value,
        title = "Wallet Withdrawal",
        ibanValue = withdrawalIbanValue,
        onIbanChange = { withdrawalIbanValue = it },
        moneyValue = withdrawalAmount,
        onMoneyChange = { withdrawalAmount = it }
    )
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
            modifier = Modifier
                .wrapContentHeight()
                .width(245.dp),
            {
                Input(
                    value = ibanValue,
                    onChange = onIbanChange,
                    placeholder = "IBAN",
                    isError = mistakeInIbanField,
                    tipMessage = "Ensure your IBAN " +
                            "contains 2 letters and 2 digits in the beginning and " +
                            "up to 26 alphanumeric characters after. " +
                            "Example: FI211234569876543210"
                )
            },
            {
                Input(
                    value = moneyValue,
                    onChange = onMoneyChange,
                    placeholder = "How much",
                    isError = mistakeInMoneyField,
                    tipMessage = "This field must contain only digits. Example: 500.50"
                )
            }
        )
    }
}

/**
 * UI model for the `WalletPage`.
 */
public class WalletPageModel(private val client: DesktopClient) {
    private var replenishmentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var withdrawalState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val balanceSubscription: EntitySubscription<WalletBalance> =
        EntitySubscription(WalletBalance::class.java, client, client.wallet())
    private val paymentError: MutableStateFlow<Boolean> =
        MutableStateFlow(false)
    private val paymentErrorMessage: MutableStateFlow<String> =
        MutableStateFlow("")

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
     * Returns the current state of the payment error, whether it is present or not.
     */
    public fun paymentError(): StateFlow<Boolean> {
        return paymentError
    }

    /**
     * Returns the current state of payment error message.
     */
    public fun paymentErrorMessage(): StateFlow<String> {
        return paymentErrorMessage
    }

    /**
     * Disables the payment error visibility and clears its message.
     */
    public fun closePaymentError() {
        paymentError.value = false
        paymentErrorMessage.value = ""
    }

    /**
     * Enables the payment error visibility
     * and sets the provided message as the payment error message.
     */
    private fun showPaymentError(message: String) {
        paymentError.value = true
        paymentErrorMessage.value = message
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
     * Sends the `WithdrawMoney` command to the server.
     *
     * @param ibanValue the IBAN of the user
     * @param moneyAmount how much to withdraw from the wallet
     */
    public fun withdrawMoney(ibanValue: String, moneyAmount: String) {
        val withdrawMoney = WithdrawMoney
            .newBuilder()
            .buildWith(ibanValue, moneyAmount)
        val withdrawalId = withdrawMoney.withdrawalProcess
        subscribeToMoneyWithdrawn(withdrawalId)
        subscribeToInsufficientFunds(withdrawalId)
        subscribeToWithdrawalError(withdrawalId)
        client.command(withdrawMoney)
    }

    /**
     * Subscribes to the `WalletReplenished` event.
     *
     * @param id the ID of the replenishment process
     */
    private fun subscribeToWalletReplenished(id: ReplenishmentId) {
        val replenishmentIdField = WalletReplenished.Field.replenishment()
        client.subscribeOnce(
            WalletReplenished::class.java,
            EventFilter.eq(replenishmentIdField, id)
        ) {
            closePaymentError()
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
            EventFilter.eq(replenishmentIdField, id)
        ) {
            showPaymentError(
                "An error occurred in payment system while the wallet was replenished."
            )
        }
    }

    /**
     * Subscribes to the `MoneyWithdrawn` event.
     */
    private fun subscribeToMoneyWithdrawn(id: WithdrawalId) {
        val withdrawalIdField = MoneyWithdrawn.Field.withdrawalProcess()
        client
            .subscribeOnce(
                MoneyWithdrawn::class.java,
                EventFilter.eq(withdrawalIdField, id)
            ) {
                closePaymentError()
            }
    }

    /**
     * Subscribes to the `InsufficientFunds` event, which signals that the amount of money
     * requested for withdrawal exceeds the available amount of money in the balance.
     */
    private fun subscribeToInsufficientFunds(id: WithdrawalId) {
        val withdrawalIdField = InsufficientFunds.Field.operation().withdrawal()
        client
            .subscribeOnce(
                InsufficientFunds::class.java,
                EventFilter.eq(withdrawalIdField, id)
            ) {
                showPaymentError(
                    "There is insufficient funds on your balance for such operation."
                )
            }
    }

    /**
     * Subscribes to the `MoneyCannotBeTransferredToUser` event that signals about
     * failure in the payment system, during the money withdrawal process.
     */
    private fun subscribeToWithdrawalError(id: WithdrawalId) {
        val withdrawalIdField = MoneyCannotBeTransferredToUser.Field.withdrawalProcess()
        client
            .subscribeOnce(
                MoneyCannotBeTransferredToUser::class.java,
                EventFilter.eq(withdrawalIdField, id)
            ) {
                showPaymentError(
                    "An error occurred in payment system while the wallet was withdrawn."
                )
            }
    }

    /**
     * Returns the command to replenish the wallet.
     *
     * @param ibanValue the IBAN of the user
     * @param amount the amount of money to replenish the wallet
     */
    private fun ReplenishWallet.Builder.buildWith(
        ibanValue: String,
        amount: String
    ): ReplenishWallet {
        return this
            .setReplenishment(ReplenishmentId.generate())
            .setWallet(client.wallet())
            .setIban(ibanValue.asIban())
            .setMoneyAmount(amount.asUsd())
            .vBuild()
    }

    /**
     * Returns the command to withdraw money from the wallet.
     *
     * @param ibanValue the IBAN of the user
     * @param amount how much to withdraw from the wallet
     */
    private fun WithdrawMoney.Builder.buildWith(ibanValue: String, amount: String): WithdrawMoney {
        return this
            .setWithdrawalProcess(WithdrawalId.generate())
            .setWallet(client.wallet())
            .setRecipient(ibanValue.asIban())
            .setAmount(amount.asUsd())
            .vBuild()
    }
}
