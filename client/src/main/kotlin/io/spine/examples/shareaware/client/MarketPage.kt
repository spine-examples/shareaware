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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.common.base.Preconditions.*
import io.spine.client.EventFilter.*
import io.spine.examples.shareaware.MoneyCalculator.*
import io.spine.examples.shareaware.PurchaseId
import io.spine.examples.shareaware.ShareId
import io.spine.examples.shareaware.client.PriceDifference.PriceDifferenceCard
import io.spine.examples.shareaware.client.PriceDifference.definePriceDifferenceConfig
import io.spine.examples.shareaware.client.payment.Dialog
import io.spine.examples.shareaware.client.wallet.Input
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
public class MarketPageModel(client: DesktopClient) {
    private val previousShares: MutableStateFlow<AvailableMarketShares?> = MutableStateFlow(null)
    private val sharesSubscriptions: EntitySubscription<AvailableMarketShares> =
        EntitySubscription(
            AvailableMarketShares::class.java, client, MarketProcess.ID
        ) { state -> previousShares.value = state }
    private val selectedShareId: MutableStateFlow<ShareId?> = MutableStateFlow(null)
    public val purchaseOperation: PurchaseOperation = PurchaseOperation(client)

    /**
     * Returns the current state of available shares on the market.
     *
     * The returnable value may contain null when the market closed or
     * when an error occurred in the market system.
     */
    public fun shares(): StateFlow<AvailableMarketShares?> {
        return sharesSubscriptions.state()
    }

    public fun selectedShare(share: ShareId) {
        selectedShareId.value = share
    }

    public fun selectedShare(): StateFlow<ShareId?> {
        return selectedShareId
    }

    public fun previousShares(): StateFlow<AvailableMarketShares?> {
        return previousShares
    }

    /**
     * Controls the state of the shares purchase operation.
     */
    public class PurchaseOperation(private val client: DesktopClient) {
        private val inProgressState: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private val shareState: MutableStateFlow<Share?> = MutableStateFlow(null)
        private val quantityState: MutableStateFlow<Int> = MutableStateFlow(0)
        private val resultMessageShownState: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private val resultMessageState: MutableStateFlow<String> = MutableStateFlow("")
        private val isFailedState: MutableStateFlow<Boolean> = MutableStateFlow(false)

        /**
         * Initiates the purchase.
         *
         * @param share the share to purchase
         */
        public fun initiate(share: Share) {
            inProgressState.value = true
            shareState.value = share
        }

        /**
         * Returns the state of the purchase operation is it in progress or not.
         */
        public fun isInProgress(): StateFlow<Boolean> {
            return inProgressState
        }

        /**
         * Sends the `PurchaseShares` command to the server.
         */
        public fun complete() {
            val share = shareState.value
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
         * Cancels the purchase operation.
         */
        public fun cancel() {
            inProgressState.value = false
            quantityState.value = 0
        }

        /**
         * Returns the share user wants to purchase.
         */
        public fun share(): StateFlow<Share?> {
            return shareState
        }

        /**
         * Sets the quantity of shares user wants to purchase.
         */
        public fun quantityOfShares(): StateFlow<Int> {
            return quantityState
        }

        /**
         * Returns the quantity of shares user wants to purchase.
         */
        public fun quantityOfShares(quantity: Int) {
            quantityState.value = quantity
        }

        /**
         * Returns the state of the message about the purchase operation result visibility.
         */
        public fun isResultMessageShown(): StateFlow<Boolean> {
            return resultMessageShownState
        }

        /**
         * Returns the current state of the message that signals about the purchase operation result.
         */
        public fun resultMessage(): StateFlow<String> {
            return resultMessageState
        }

        /**
         * Returns the current state of the purchase operation, whether it failed or not.
         */
        public fun isFailed(): StateFlow<Boolean> {
            return isFailedState
        }

        /**
         * Enables the visibility of the message about successful purchase operation
         * and sets the provided message to it.
         */
        private fun showSuccessfulOperationMessage(message: String) {
            resultMessageShownState.value = true
            isFailedState.value = false
            resultMessageState.value = message
        }

        /**
         * Enables the visibility of the message about failed purchase operation
         * and sets the provided message to it.
         */
        private fun showFailedOperationMessage(message: String) {
            resultMessageShownState.value = true
            isFailedState.value = true
            resultMessageState.value = message
        }

        /**
         * Disables the visibility of the message about purchase operation result
         * and clears its message.
         */
        public fun closeOperationResultMessage() {
            resultMessageShownState.value = false
            isFailedState.value = false
            resultMessageState.value = ""
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
                showSuccessfulOperationMessage("Shares have been purchased successfully.")
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
                showFailedOperationMessage(
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
                showFailedOperationMessage(
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
                .setQuantity(quantityState.value)
                .setShare(share.id)
                .vBuild()
        }
    }
}

/**
 * The page component that provides data about currently available shares on the market
 * and ways to interact with them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun MarketPage(model: MarketPageModel) {
    val popUpShown = model.purchaseOperation.isResultMessageShown().collectAsState()
    val popUpMessage = model.purchaseOperation.resultMessage().collectAsState()
    val popUpInErrorState = model.purchaseOperation.isFailed().collectAsState()
    val popUpContentColor = if (popUpInErrorState.value) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary
    Scaffold(
        bottomBar = {
            PopUpMessage(
                isShown = popUpShown.value,
                dismissAction = { model.purchaseOperation.closeOperationResultMessage() },
                label = popUpMessage.value,
                contentColor = popUpContentColor,
                modifier = Modifier
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
            val purchaseInProgress = model.purchaseOperation.isInProgress().collectAsState()
            PurchaseDialog(
                model = model,
                isShown = purchaseInProgress.value
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
    val shareToPurchase = model.purchaseOperation.share().collectAsState()
    val quantity = model.purchaseOperation.quantityOfShares().collectAsState()
    if (isShown) {
        Dialog(
            onCancel = {
                scope.launch {
                    model.purchaseOperation.cancel()
                }
            },
            onConfirm = {
                scope.launch {
                    model.purchaseOperation.complete()
                    model.purchaseOperation.cancel()
                }
            },
            title = "Purchase '${shareToPurchase.value?.companyName}' shares",
            inputs = arrayOf({
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
            })
        )
    }
}

/**
 * Represents the input component accepting only the numeric values.
 */
@Composable
private fun NumericInput(model: MarketPageModel) {
    val input = remember { mutableStateOf("") }
    val onChange: (String) -> Unit = {
        if (it.validateNumber()) {
            input.value = it
            val quantity = if (it == "") 0 else it.toInt()
            model.purchaseOperation.quantityOfShares(quantity)
        }
    }
    Input(
        value = input.value,
        onChange = onChange,
        placeholder = "How much to purchase",
        isError = false
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
private fun MainItemContent(share: Share, previousShare: Share?) {
    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(2F)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(share.companyName, style = MaterialTheme.typography.bodySmall)
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(share.price.asReadableString(), style = MaterialTheme.typography.headlineSmall)
            val priceDifference = definePriceDifferenceConfig(share.price, previousShare?.price)
            PriceDifferenceCard(priceDifference)
        }
    }
}

/**
 * Provides API to define and show the difference between two `Money` objects.
 */
private object PriceDifference {

    /**
     * Configuration for the `PriceDifferenceCard` component.
     */
    data class PriceDifferenceConfig(val color: Color, val price: String)

    /**
     * Defines the `PriceDifferenceConfig` taking two provided `Money` objects.
     */
    @Composable
    fun definePriceDifferenceConfig(actualPrice: Money, previousPrice: Money?): PriceDifferenceConfig {
        val color: Color
        val price: String
        if (null == previousPrice) {
            color = definePriceDifferenceColor(actualPrice, actualPrice)
            price = actualPrice.subtract(actualPrice)
        } else {
            color = definePriceDifferenceColor(actualPrice, previousPrice)
            price = previousPrice.subtract(actualPrice)
        }
        return PriceDifferenceConfig(color, price)
    }

    /**
     * Defines the color to use as a background for the `PriceDifferenceCard` component.
     */
    @Composable
    private fun definePriceDifferenceColor(actualPrice: Money, previousPrice: Money): Color {
        return if (isGreater(actualPrice, previousPrice))
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.error
    }

    /**
     * Represents the card that shows difference between two `Money` objects.
     */
    @Composable
    fun PriceDifferenceCard(priceDifferenceConfig: PriceDifferenceConfig) {
        val (color, price) = priceDifferenceConfig
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
}

/**
 * Represents the search field.
 */
@Composable
private fun SearchField(
    value: String,
    onChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        Input(
            value = value,
            onChange = onChange,
            placeholder = "Search",
            isError = false,
            containerColor = MaterialTheme.colorScheme.secondary,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 5.dp)
                )
            }
        )
    }
}

private fun Money.subtract(money: Money): String {
    return if (isGreater(this, money)) {
        val result = subtract(this, money)
        "-${result.asReadableString()}"
    } else {
        val result = subtract(money, this)
        "+${result.asReadableString()}"
    }
}

/**
 * Represents the share icon.
 */
@Composable
private fun ShareIcon(companyLogo: String, companyName: String) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            load = { loadImage(companyLogo, density) },
            painterFor = { it },
            contentDescription = companyName,
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
                model.purchaseOperation.initiate(share)
            }
        },
        "Buy",
        modifier = Modifier
            .width(110.dp)
            .height(40.dp)
    )
}

/**
 * Draws an image.
 *
 * @param load callback that loads the image
 * @param painterFor painter for the image
 * @param contentDescription what the image represents
 * @param modifier modifier used to adjust layout
 * @param contentScale scale parameter used to determine the aspect ratio scaling
 */
@Composable
private fun <T> Image(
    load: () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Inside,
) {
    val image: T? = try {
        load()
    } catch (e: IOException) {
        throw illegalArgumentWithCauseOf(e)
    }
    if (image != null) {
        Image(
            painter = painterFor(image),
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
