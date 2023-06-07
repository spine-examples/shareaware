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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import com.google.common.base.Preconditions
import io.spine.client.EventFilter.*
import io.spine.examples.shareaware.MoneyCalculator
import io.spine.examples.shareaware.PurchaseId
import io.spine.examples.shareaware.client.payment.Dialog
import io.spine.examples.shareaware.investment.command.PurchaseShares
import io.spine.examples.shareaware.market.AvailableMarketShares
import io.spine.examples.shareaware.server.market.MarketProcess
import io.spine.examples.shareaware.share.Share
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

    /**
     * Returns the current state of available shares on the market.
     */
    public fun shares(): StateFlow<AvailableMarketShares?> {
        return sharesSubscriptions.state()
    }

    public fun toPurchaseState(share: Share) {
        purchaseState.value = true
        shareToPurchase.value = share
    }

    public fun toDefaultState() {
        purchaseState.value = false
        quantityToPurchase.value = 0
    }

    public fun purchaseState(): StateFlow<Boolean> {
        return purchaseState
    }

    public fun shareToPurchase(): StateFlow<Share?> {
        return shareToPurchase
    }

    public fun quantityToPurchase(quantity: Int) {
        quantityToPurchase.value = quantity
    }

    public fun quantityToPurchase(): StateFlow<Int> {
        return quantityToPurchase
    }

    public fun purchaseShares() {
        val share = shareToPurchase.value
        Preconditions.checkNotNull(share)
        val purchaseShares = PurchaseShares
            .newBuilder()
            .buildWith(share!!)
        client.command(purchaseShares)
    }

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumericInput(model: MarketPageModel) {
    val input = remember { mutableStateOf("") }
    val change: (String) -> Unit = {
        if (it.validateQuantity()) {
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

private fun String.validateQuantity(): Boolean {
    val numericRegex = """^(?!0)[0-9]*${'$'}""".toRegex()
    return numericRegex.containsMatchIn(this)
}

private fun calculatePrice(pricePerOne: Money?, quantity: Int): String {
    Preconditions.checkArgument(null != pricePerOne)
    val totalPrice = MoneyCalculator.multiply(pricePerOne, quantity)
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
