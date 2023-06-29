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

package io.spine.examples.shareaware.client.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.client.PrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Configures the `PopupComponent`.
 *
 * @param isShown is a component shown to the user
 * @param dismissAction callback that will be triggered when the user clicks on `Cancel` button
 * @param label the message to be shown to the user
 * @param contentColor the preferred color for content inside this pop-up
 */
public data class PopupConfig(
    val isShown: Boolean,
    val dismissAction: () -> Unit,
    val label: String,
    val contentColor: Color
)

/**
 * Displays a popup.
 *
 * @param popupConfig configuration of the popup
 */
@Composable
public fun Popup(
    popupConfig: PopupConfig
) {
    val (isShown, dismissAction, label, contentColor) = popupConfig
    if (isShown) {
        Card(
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier.wrapContentWidth(),
        ) {
            Row(
                modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
                Spacer(modifier = Modifier.width(15.dp))
                CloseButton { dismissAction() }
            }
        }
    }
}

/**
 * Displays a button that closes the `Popup` component.
 */
@Composable
private fun CloseButton(
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope { Dispatchers.Default }
    PrimaryButton(
        onClick = {
            scope.launch {
                onClick()
            }
        },
        "Close",
        modifier = Modifier
            .width(70.dp)
            .height(20.dp),
        labelStyle = MaterialTheme.typography.bodySmall,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(2.dp),
        color = MaterialTheme.colorScheme.secondary
    )
}
