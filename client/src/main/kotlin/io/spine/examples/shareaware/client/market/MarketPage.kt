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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * The page component that provides data about currently available shares on the market
 * and ways to interact with them.
 */
@Composable
public fun MarketPage(model: MarketPageModel) {
    val marketShares by model.shares().collectAsState()
    val previousShares by model.previousShares().collectAsState()
    val selectedShareId by model.selectedShare().collectAsState()
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        SharesTab(
            marketShares = marketShares?.shareList,
            previousShares = previousShares?.shareList,
            onShareSelect = {
                model.selectedShare(it)
            }
        )
        if (selectedShareId != null) {
            val selectedShare = marketShares?.shareList
                ?.find { share -> share.id == selectedShareId }
            val previousShare = previousShares?.shareList
                ?.find { share -> share.id == selectedShareId }
            ShareProfileTab(
                selectedShare = selectedShare!!,
                previousPrice = previousShare?.price,
                purchaseModel = model.purchaseOperationModel
            )
        } else {
            EmptyShareProfileTab()
        }
        PurchaseDialog (model.purchaseOperationModel)
    }
}
