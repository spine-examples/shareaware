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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Provides current page state and menu component configuration.
 */
public object Navigation {
    public val currentPage: StateFlow<Page> = CurrentPage.state()
    public val items: List<MenuItem> = listOf(
        MenuItem(
            "Home",
            Icons.HOME
        ) { CurrentPage.home() },
        MenuItem(
            "Wallet",
            Icons.WALLET
        ) { CurrentPage.wallet() },
        MenuItem(
            "Market",
            Icons.MARKET
        ) { CurrentPage.market() },
        MenuItem(
            "Investments",
            Icons.INVESTMENT
        ) { CurrentPage.investments() },
        MenuItem(
            "Watchlists",
            Icons.WATCHLIST
        ) { CurrentPage.watchlists() }
    )
}

/**
 * Provides pages of the application.
 */
public enum class Page {
    HOME, WALLET, MARKET, INVESTMENTS, WATCHLISTS;
}

/**
 * Represents the item of the menu.
 */
public data class MenuItem(val name: String,
                           val iconPath: String,
                           val toPage: () -> Unit)

/**
 * Component that represents the menu for navigating through the application.
 */
@Composable
public fun MenuLayout(items: List<MenuItem>) {
    var selectedItem by remember { mutableStateOf(0) }
    NavigationRail(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                modifier = Modifier
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        painterResource(item.iconPath),
                        contentDescription = item.name,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                },
                label = {
                    Text(
                        item.name, style = MaterialTheme.typography.bodyMedium
                    )
                },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    item.toPage()
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

/**
 * The current page of the application.
 */
private object CurrentPage {

    private val currentPage: MutableStateFlow<Page> = MutableStateFlow(Page.HOME)

    /**
     * Represents the current page as a read-only state flow.
     */
    fun state(): StateFlow<Page> {
        return currentPage.asStateFlow()
    }

    /**
     * Changes the current page to the "Home" page.
     */
    fun home() {
        currentPage.value = Page.HOME
    }

    /**
     * Changes the current page to the "Wallet" page.
     */
    fun wallet() {
        currentPage.value = Page.WALLET
    }

    /**
     * Changes the current page to the "Market" page.
     */
    fun market() {
        currentPage.value = Page.MARKET
    }

    /**
     * Changes the current page to the "Investments" page.
     */
    fun investments() {
        currentPage.value = Page.INVESTMENTS
    }

    /**
     * Changes the current page to the "Watchlists" page.
     */
    fun watchlists() {
        currentPage.value = Page.WATCHLISTS
    }
}
