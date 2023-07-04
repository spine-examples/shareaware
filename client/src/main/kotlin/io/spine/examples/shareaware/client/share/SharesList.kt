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

package io.spine.examples.shareaware.client.share

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.ShareId
import io.spine.examples.shareaware.client.asReadableString
import io.spine.examples.shareaware.client.component.PriceDifferenceCard
import io.spine.examples.shareaware.share.Share
import io.spine.money.Money

/**
 * Displays the list of shares.
 *
 * @param listState the state object to be used to control or observe the list's state
 * @param shares the list of the actual market shares
 * @param previousShares the list of the previous market shares
 * @param onShareSelect the callback to be triggered when the share will be selected
 */
@Composable
public fun SharesList(
    listState: LazyListState,
    shares: List<Share>,
    previousShares: List<Share>?,
    onShareSelect: (ShareId) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        shares.forEach { share ->
            item {
                ShareItem(
                    share = share,
                    previousPrice = previousShares?.find { previousShare ->
                        previousShare.id == share.id
                    }?.price,
                    onShareSelect = onShareSelect
                )
            }
        }
    }
}


/**
 * Displays the list item with information about the share.
 *
 * @param share the share to display
 * @param previousPrice the previous price of this share
 * @param onShareSelect the callback to be triggered when the share will be selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShareItem(
    share: Share,
    previousPrice: Money?,
    onShareSelect: (ShareId) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .bottomBorder(MaterialTheme.colorScheme.onSecondary)
    ) {
        ListItem(
            modifier = Modifier
                .height(60.dp)
                .clickable(
                    enabled = true,
                    onClick = {
                        onShareSelect(share.id)
                    }
                ),
            headlineText = {
                ShareItemContent(share, previousPrice)
            },
        )
    }
}

/**
 * Displays the main `ListItem` content with data about the share.
 *
 * @param share the share to show inside the item
 * @param previousPrice the previous price of this share
 */
@Composable
private fun ShareItemContent(
    share: Share,
    previousPrice: Money?
) {
    Row(
        modifier = Modifier.fillMaxSize()
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
            PriceDifferenceCard(share.price, previousPrice)
        }
    }
}

/**
 * Extension for the `Modifier` that draws the bottom border of the component.
 */
private fun Modifier.bottomBorder(color: Color): Modifier {
    return this.drawBehind {
        drawLine(
            color = color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx(),
            alpha = 0.5f
        )
    }
}
