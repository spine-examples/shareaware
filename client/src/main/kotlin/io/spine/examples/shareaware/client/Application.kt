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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.spine.client.ConnectionConstants.*
import io.spine.examples.shareaware.client.wallet.WalletPage
import io.spine.examples.shareaware.client.wallet.WalletPageModel

/**
 * The root component of the application.
 *
 * Responsible for navigation and composition of pages.
 */
public fun application(): Unit = application {
    val client = DesktopClient.init(
        "localhost",
        DEFAULT_CLIENT_SERVICE_PORT
    )
    val walletPageModel = WalletPageModel(client)
    val marketPageModel = MarketPageModel(client)
    ShareAwareTheme {
        Window(
            onCloseRequest = ::exitApplication,
            state = WindowState(width = 1200.dp, height = 600.dp),
            title = "ShareAware"
        ) {
            val currentPage = Page.current.collectAsState()
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.width(160.dp)
                ) {
                    Menu()
                }
                when (currentPage.value) {
                    Page.HOME -> HomePage()
                    Page.WALLET -> WalletPage(walletPageModel)
                    Page.MARKET -> MarketPage(marketPageModel)
                    Page.INVESTMENTS -> InvestmentsPage()
                    Page.WATCHLISTS -> WatchlistsPage()
                }
            }
        }
    }
}
