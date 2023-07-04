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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Component that represents the menu for navigating through the application.
 */
@Composable
public fun Navigation() {
    var selectedItem by remember { mutableStateOf(0) }
    NavigationRail(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondary,
    ) {
        Page.values().forEachIndexed { index, page ->
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(30.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            ) {
                NavItem(
                    page = page,
                    isSelected = selectedItem == index
                ) {
                    selectedItem = index
                    CurrentPage.set(page)
                }
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

/**
 * Represents the navigation menu item.
 *
 * @param page the page to represent
 * @param isSelected is an item selected
 * @param onClick callback that will be triggered when the item clicked
 */
@Composable
private fun NavItem(
    page: Page,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationRailItem(
        modifier = Modifier.fillMaxWidth(),
        icon = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.width(5.dp))
                Icon(
                    painterResource(page.iconPath),
                    contentDescription = page.label,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = page.label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            indicatorColor = MaterialTheme.colorScheme.surface,
        )
    )
}

/**
 * Provides pages of the application.
 *
 * @param label the name of the page
 * @param iconPath the path to the icon that should represent the page
 */
public enum class Page(
    public val label: String,
    public val iconPath: String
) {
    HOME("Home", Icons.HOME),
    WALLET("Wallet", Icons.WALLET),
    MARKET("Market", Icons.MARKET),
    INVESTMENTS("Investments", Icons.INVESTMENT),
    WATCHLISTS("Watchlists", Icons.WATCHLIST);

    /**
     * Provides the current page of the application.
     */
    public companion object {
        public val current: StateFlow<Page> = CurrentPage.state()
    }
}

/**
 * The current page of the application.
 */
private object CurrentPage {

    private val current: MutableStateFlow<Page> = MutableStateFlow(Page.HOME)

    /**
     * Represents the current page as a read-only state flow.
     */
    fun state(): StateFlow<Page> {
        return current.asStateFlow()
    }

    /**
     * Changes the current page to the provided.
     */
    fun set(page: Page) {
        this.current.value = page
    }
}
