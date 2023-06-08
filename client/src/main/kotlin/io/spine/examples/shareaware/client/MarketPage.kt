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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.common.base.Preconditions.*
import io.spine.client.EventFilter.*
import io.spine.examples.shareaware.MoneyCalculator
import io.spine.examples.shareaware.PurchaseId
import io.spine.examples.shareaware.client.payment.Dialog
import io.spine.examples.shareaware.client.wallet.PopUpMessage
import io.spine.examples.shareaware.investment.command.PurchaseShares
import io.spine.examples.shareaware.investment.event.SharesPurchased
import io.spine.examples.shareaware.market.AvailableMarketShares
import io.spine.examples.shareaware.market.rejection.Rejections.SharesCannotBeObtained
import io.spine.examples.shareaware.server.market.MarketProcess
import io.spine.examples.shareaware.share.Share
import io.spine.examples.shareaware.wallet.rejection.Rejections.InsufficientFunds
import io.spine.money.Money
import io.spine.util.Exceptions.*
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI model for the `MarketPage`.
 */
public class MarketPageModel(private val client: DesktopClient) {
    private val sharesSubscriptions: EntitySubscription<AvailableMarketShares> =
        EntitySubscription(AvailableMarketShares::class.java, client, MarketProcess.ID)
    private val purchaseState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val shareToPurchase: MutableStateFlow<Share?> = MutableStateFlow(null)
    private val quantityToPurchase: MutableStateFlow<Int> = MutableStateFlow(0)
    private val purchaseResultMessageShown: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val purchaseResultMessage: MutableStateFlow<String> = MutableStateFlow("")
    private val isPurchaseFailed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Returns the current state of available shares on the market.
     *
     * The returnable value may contain null when the market closed or
     * when an error occurred in the market system.
     */
    public fun shares(): StateFlow<AvailableMarketShares?> {
        return sharesSubscriptions.state()
    }

    /**
     * Sets the page to "purchase" state.
     *
     * Page state when the user wants to purchase shares.
     *
     * @param share the share to purchase
     */
    public fun toPurchaseState(share: Share) {
        purchaseState.value = true
        shareToPurchase.value = share
    }

    /**
     * Sets page to default state.
     */
    public fun toDefaultState() {
        purchaseState.value = false
        quantityToPurchase.value = 0
    }

    /**
     * Returns the "purchase" state of the page.
     */
    public fun purchaseState(): StateFlow<Boolean> {
        return purchaseState
    }

    /**
     * Returns the share user wants to purchase.
     */
    public fun shareToPurchase(): StateFlow<Share?> {
        return shareToPurchase
    }

    /**
     * Sets the quantity of shares user wants to purchase.
     */
    public fun quantityToPurchase(quantity: Int) {
        quantityToPurchase.value = quantity
    }

    /**
     * Returns the quantity of shares user wants to purchase.
     */
    public fun quantityToPurchase(): StateFlow<Int> {
        return quantityToPurchase
    }

    /**
     * Returns the state of the message about the purchase operation result visibility.
     */
    public fun isPurchaseResultMessageShown(): StateFlow<Boolean> {
        return purchaseResultMessageShown
    }

    /**
     * Returns the current state of the message that signals about the purchase operation result.
     */
    public fun purchaseResultMessage(): StateFlow<String> {
        return purchaseResultMessage
    }

    /**
     * Returns the current state of the purchase operation, whether it failed or not.
     */
    public fun isPurchaseFailed(): StateFlow<Boolean> {
        return isPurchaseFailed
    }

    /**
     * Enables the visibility of the message about successful purchase operation
     * and sets the provided message to it.
     */
    private fun showSuccessfulPurchaseMessage(message: String) {
        purchaseResultMessageShown.value = true
        isPurchaseFailed.value = false
        purchaseResultMessage.value = message
    }

    /**
     * Enables the visibility of the message about failed purchase operation
     * and sets the provided message to it.
     */
    private fun showFailedPurchasedMessage(message: String) {
        purchaseResultMessageShown.value = true
        isPurchaseFailed.value = true
        purchaseResultMessage.value = message
    }

    /**
     * Disables the visibility of the message about purchase operation result
     * and clears its message.
     */
    public fun closePurchaseResultMessage() {
        purchaseResultMessageShown.value = false
        isPurchaseFailed.value = false
        purchaseResultMessage.value = ""
    }

    /**
     * Sends the `PurchaseShares` command to the server.
     */
    public fun purchaseShares() {
        val share = shareToPurchase.value
        checkNotNull(share)
        val purchaseShares = PurchaseShares
            .newBuilder()
            .buildWith(share!!)
        val purchaseProcess = purchaseShares.purchaseProcess
        subscribeToSharesPurchased(purchaseProcess)
        subscribeToInsufficientFunds(purchaseProcess)
        subscribeToSharesCannotBeObtained(purchaseProcess)
        client.command(purchaseShares)
    }

    /**
     * Subscribes to the `SharesPurchased` event.
     */
    private fun subscribeToSharesPurchased(id: PurchaseId) {
        val purchaseIdField = SharesPurchased.Field.purchaseProcess()
        client.subscribeOnce(
            SharesPurchased::class.java,
            eq(purchaseIdField, id)
        ) {
            showSuccessfulPurchaseMessage("Shares have been purchased successfully.")
        }
    }

    /**
     * Subscribes to the `InsufficientFunds` event, which signals that the price of purchase
     * exceeds the amount of available funds on the balance.
     */
    private fun subscribeToInsufficientFunds(id: PurchaseId) {
        val purchaseIdField = InsufficientFunds.Field.operation().purchase()
        client.subscribeOnce(
            InsufficientFunds::class.java,
            eq(purchaseIdField, id)
        ) {
            showFailedPurchasedMessage(
                "There are insufficient funds on your balance to purchase those shares."
            )
        }
    }

    /**
     * Subscribes to the `SharesCannotBeObtained` event that signals about error in the market,
     * when trying to purchase shares.
     */
    private fun subscribeToSharesCannotBeObtained(id: PurchaseId) {
        val purchaseIdField = SharesCannotBeObtained.Field.purchaseProcess()
        client.subscribeOnce(
            SharesCannotBeObtained::class.java,
            eq(purchaseIdField, id)
        ) {
            showFailedPurchasedMessage(
                "An error occurred in the market system while shares were purchasing."
            )
        }
    }

    /**
     * Returns the command to purchase shares.
     *
     * @param share the share to purchase
     */
    private fun PurchaseShares.Builder.buildWith(share: Share): PurchaseShares {
        return this
            .setPurchaseProcess(PurchaseId.generate())
            .setPurchaser(client.authenticatedUser())
            .setPrice(share.price)
            .setQuantity(quantityToPurchase.value)
            .setShare(share.id)
            .vBuild()
    }
}

/**
 * The page component that provides data about currently available shares on the market
 * and ways to interact with them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MarketPage(model: MarketPageModel) {
    val popUpShown = model.isPurchaseResultMessageShown().collectAsState()
    val popUpMessage = model.purchaseResultMessage().collectAsState()
    val popUpInErrorState = model.isPurchaseFailed().collectAsState()
    val popUpContentColor = if (popUpInErrorState.value) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary
    Scaffold(
        bottomBar = {
            PopUpMessage(
                isShown = popUpShown.value,
                dismissAction = { model.closePurchaseResultMessage() },
                label = popUpMessage.value,
                contentColor = popUpContentColor,
                modifier = Modifier
                    .requiredWidthIn(200.dp, 700.dp)
                    .wrapContentWidth()
                    .zIndex(1f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .zIndex(0f)
        ) {
            val marketShares by model.shares().collectAsState()
            marketShares?.shareList?.forEach { share ->
                ListItem(
                    modifier = Modifier
                        .height(100.dp),
                    headlineText = {
                        MainItemContent(share)
                    },
                    leadingContent = {
                        ShareIcon(share)
                    },
                    trailingContent = {
                        ButtonSection(model, share)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                Divider(
                    thickness = 2.dp
                )
            }
            val purchaseState = model.purchaseState().collectAsState()
            PurchaseDialog(
                model = model,
                isShown = purchaseState.value
            )
        }
    }
}

/**
 * Represents the dialog window for the shares purchase purposes.
 */
@Composable
private fun PurchaseDialog(
    model: MarketPageModel,
    isShown: Boolean
) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    val shareToPurchase = model.shareToPurchase().collectAsState()
    val quantity = model.quantityToPurchase().collectAsState()
    if (isShown) {
        Dialog(
            onCancel = {
                scope.launch {
                    model.toDefaultState()
                }
            },
            onConfirm = {
                scope.launch {
                    model.purchaseShares()
                    model.toDefaultState()
                }
            },
            title = "Purchase '${shareToPurchase.value?.companyName}' shares",
            {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 20.dp, top = 10.dp)
                ) {
                    val price = calculatePrice(shareToPurchase.value?.price, quantity.value)
                    Text(
                        "Total Price - $price",
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                    NumericInput(model)
                }
            },
        )
    }
}

/**
 * Represents the input component accepting only the numeric values.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumericInput(model: MarketPageModel) {
    val input = remember { mutableStateOf("") }
    val change: (String) -> Unit = {
        if (it.validateNumber()) {
            input.value = it
            val quantity = if (it == "") 0 else it.toInt()
            model.quantityToPurchase(quantity)
        }
    }
    TextField(
        value = input.value,
        modifier = Modifier.fillMaxWidth(),
        onValueChange = change,
        label = {
            Text("How much to purchase")
        }
    )
}

/**
 * Returns true if this `String` is written like a number, false otherwise.
 */
private fun String.validateNumber(): Boolean {
    val numericRegex = """^(?!0)[0-9]*${'$'}""".toRegex()
    return numericRegex.containsMatchIn(this)
}

/**
 * Calculates the price of the purchase operation taking
 * the quantity of shares and price per one share.
 *
 * @return the readable string that represents the total price of the purchase.
 */
private fun calculatePrice(pricePerOne: Money?, quantity: Int): String {
    checkArgument(null != pricePerOne)
    val totalPrice = MoneyCalculator.multiply(pricePerOne!!, quantity)
    return totalPrice.asReadableString()
}

/**
 * Represents the main `ListItem` content with data about the share.
 */
@Composable
private fun MainItemContent(share: Share) {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(share.companyName)
        }
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(share.price.asReadableString())
        }
    }
}

/**
 * Represents the share icon.
 */
@Composable
private fun ShareIcon(share: Share) {
    val density = LocalDensity.current
    Row(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            load = { loadImage(share.companyLogo, density) },
            painterFor = { it },
            contentDescription = share.companyName,
        )
    }
}

/**
 * Represents the button section.
 */
@Composable
private fun ButtonSection(model: MarketPageModel, share: Share) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    PrimaryButton(
        onClick = {
            scope.launch {
                model.toPurchaseState(share)
            }
        },
        "Buy",
        modifier = Modifier
            .width(110.dp)
            .height(40.dp)
    )
}

/**
 * Asynchronously draws an image.
 *
 * @param load callback that loads the image
 * @param painterFor painter for the image
 * @param contentDescription what the image represents
 * @param modifier modifier used to adjust layout
 * @param contentScale scale parameter used to determine the aspect ratio scaling
 */
@Composable
private fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val image: T? by produceState<T?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                throw illegalArgumentWithCauseOf(e)
            }
        }
    }
    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

/**
 * Loads an image from the network by URL.
 *
 * @param url the URL of the image to load
 * @param density density that will be used to set the intrinsic size of the image
 * @return the decoded SVG image associated with the URL
 */
private fun loadImage(url: String, density: Density): Painter =
    URL(url).openStream().buffered().use { loadSvgPainter(it, density) }

/**
 * Returns the readable `String` constructed from the `Money` object.
 */
private fun Money.asReadableString(): String {
    return "$" + this.units.toString() + "." + this.nanos.toString()
}
