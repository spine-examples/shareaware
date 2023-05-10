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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.spine.examples.shareaware.client.wallet.WalletPage

/**
 * The root component of the application.
 *
 * Responsible for navigation and composition of pages.
 */
fun application() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(width = 1200.dp, height = 600.dp),
        title = "ShareAware"
    ) {
        var page by remember { mutableStateOf(Pages.HOME) }
        val items = mapOf(
            Pages.HOME to MenuItem(
                "Home",
                Icons.HOME
            ) { page = Pages.HOME },
            Pages.WALLET to MenuItem(
                "Wallet",
                Icons.WALLET
            ) { page = Pages.WALLET },
            Pages.MARKET to MenuItem(
                "Market",
                Icons.MARKET
            ) { page = Pages.MARKET },
            Pages.INVESTMENTS to MenuItem(
                "Investments",
                Icons.INVESTMENT
            ) { page = Pages.INVESTMENTS },
            Pages.WATCHLISTS to MenuItem(
                "Watchlists",
                Icons.WATCHLIST
            ) { page = Pages.WATCHLISTS }
        )
        Row(
            modifier = Modifier
                .background(Colors.DARKBEIGE)
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.width(150.dp)
            ) {
                Logo()
                Menu(items, page)
            }
            when (page) {
                Pages.HOME -> Text("HOME")
                Pages.WALLET -> WalletPage()
                Pages.MARKET -> Text("MARKET")
                Pages.INVESTMENTS -> Text("INVESTMENTS")
                Pages.WATCHLISTS -> Text("WATCHLISTS")
            }
        }
    }
}
