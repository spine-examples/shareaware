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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.client.component.Image
import io.spine.examples.shareaware.client.component.PriceDifferenceCard
import io.spine.examples.shareaware.client.PrimaryButton
import io.spine.examples.shareaware.client.component.Scaffold
import io.spine.examples.shareaware.client.component.PopupConfig
import io.spine.examples.shareaware.client.extension.asReadableString
import io.spine.examples.shareaware.share.Share
import io.spine.money.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays a tab for interacting with the selected share.
 */
@Composable
public fun ShareProfileTab(
    selectedShare: Share,
    previousPrice: Money?,
    purchaseModel: PurchaseOperationModel
) {
    val popUpShown by purchaseModel.isResultMessageShown().collectAsState()
    val popUpMessage by purchaseModel.resultMessage().collectAsState()
    val popUpInErrorState by purchaseModel.isFailed().collectAsState()
    val popUpContentColor = if (popUpInErrorState) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        popupConfig = PopupConfig(
            isShown = popUpShown,
            dismissAction = { purchaseModel.closeOperationResultMessage() },
            label = popUpMessage,
            contentColor = popUpContentColor
        )
    ) {
        ShareProfile(
            share = selectedShare,
            previousPrice = previousPrice,
            purchaseModel = purchaseModel
        )
    }
}

/**
 * Displays an empty share profile tab.
 */
@Composable
public fun EmptyShareProfileTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Select the share",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

/**
 * Displays the main information about the share and ways to interact with it.
 *
 * @param share the share to represent
 * @param previousPrice the previous price of this share
 * @param purchaseModel the model of the shares purchase operation
 */
@Composable
private fun ShareProfile(
    share: Share,
    previousPrice: Money?,
    purchaseModel: PurchaseOperationModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShareLogo(share)
        Text(
            text = share.companyName,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 10.dp)
        )
        SharePrice(share.price, previousPrice)
        ButtonSection(
            share = share,
            purchaseModel = purchaseModel
        )
    }
}

/**
 * Displays information about the share price.
 *
 * @param actualPrice the actual share price to be shown
 * @param previousPrice the previous price of this share to show difference with
 */
@Composable
private fun SharePrice(
    actualPrice: Money,
    previousPrice: Money?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 10.dp)
            .wrapContentHeight()
    ) {
        Text(
            text = actualPrice.asReadableString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(end = 10.dp)
        )
        PriceDifferenceCard(actualPrice, previousPrice)
    }
}

/**
 * Draws the share logo.
 *
 * @param share the share which logo to draw
 */
@Composable
private fun ShareLogo(share: Share) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .size(150.dp)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            url = share.companyLogo,
            density = density,
            contentDescription = share.companyName,
        )
    }
}

/**
 * Displays the button section.
 *
 * @param share the share with which to interact with buttons
 * @param purchaseModel the model of the shares purchase operation
 */
@Composable
private fun ButtonSection(
    share: Share,
    purchaseModel: PurchaseOperationModel
) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .width(230.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PrimaryButton(
            onClick = {},
            label = "Sell",
            modifier = Modifier
                .width(110.dp)
                .height(35.dp),
            labelStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondary
        )
        PrimaryButton(
            onClick = {
                scope.launch {
                    purchaseModel.initiate(share)
                }
            },
            label = "Buy",
            modifier = Modifier
                .width(110.dp)
                .height(35.dp),
            labelStyle = MaterialTheme.typography.bodyMedium,
            shape = MaterialTheme.shapes.small
        )
    }
}
