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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the item of the menu.
 */
public data class MenuItem(val name: String,
                           val iconPath: String,
                           val toPage: () -> Unit)

/**
 * The current page of the application.
 */
private object CurrentPage {
    private val currentPage: MutableStateFlow<Pages> = MutableStateFlow(Pages.HOME)

    fun changesTo(page: Pages) {
        currentPage.value = page
    }

    fun asStateFlow(): StateFlow<Pages> {
        return currentPage.asStateFlow()
    }
}

/**
 * Provides menu state and configuration.
 */
public object MenuModel {
    public val currentPage: StateFlow<Pages> = CurrentPage.asStateFlow()
    public val items: Map<Pages, MenuItem> = mapOf(
        Pages.HOME to MenuItem(
            "Home",
            Icons.HOME
        ) { CurrentPage.changesTo(Pages.HOME)},
        Pages.WALLET to MenuItem(
            "Wallet",
            Icons.WALLET
        ) { CurrentPage.changesTo(Pages.WALLET)},
        Pages.MARKET to MenuItem(
            "Market",
            Icons.MARKET
        ) { CurrentPage.changesTo(Pages.MARKET)},
        Pages.INVESTMENTS to MenuItem(
            "Investments",
            Icons.INVESTMENT
        ) { CurrentPage.changesTo(Pages.INVESTMENTS)},
        Pages.WATCHLISTS to MenuItem(
            "Watchlists",
            Icons.WATCHLIST
        ) { CurrentPage.changesTo(Pages.WATCHLISTS) }
    )
}

/**
 * The component that represents the menu for navigation through [Pages].
 */
@Composable
public fun MenuView(items: Map<Pages, MenuItem>, currentPage: Pages) {
    NavigationRail(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        items.entries.forEach { item ->
            NavigationRailItem(
                modifier = Modifier
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        painterResource(item.value.iconPath),
                        contentDescription = item.value.name,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                },
                label = {
                    Text(
                        item.value.name, style = MaterialTheme.typography.bodyMedium
                    )
                },
                selected = currentPage == item.key,
                onClick = {
                    item.value.toPage()
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
