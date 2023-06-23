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
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.client.payment.Tooltip

/**
 * The input component that supports displaying a tip.
 *
 * @param value the input text to be shown in the text field
 * @param onChange the callback that is triggered when the input's value change
 * @param placeholder the label to be displayed inside the input container
 * @param isError indicates if the input's current value is in error
 * @param tipMessage message to be displayed in the tooltip
 * @param containerColor the color used for the background of this input
 * @param leadingIcon the optional leading icon to be displayed at the beginning of the input field container
 */
@Composable
public fun Input(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    tipMessage: String = "",
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Unspecified
    val toolTipIconColor = if (isError) MaterialTheme.colorScheme.error else
        MaterialTheme.colorScheme.onSecondary
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = MaterialTheme.typography.bodySmall,
        interactionSource = interactionSource,
        maxLines = 1
    ) { innerTextField ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = MaterialTheme.shapes.small
                )
                .background(
                    color = containerColor,
                    shape = MaterialTheme.shapes.small
                )
                .padding(
                    start = if (leadingIcon == null) 16.dp else 5.dp,
                    end = 16.dp,
                    top = if (leadingIcon == null) 8.dp else 2.dp,
                    bottom = if (leadingIcon == null) 8.dp else 2.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
                innerTextField()
            }
            if (tipMessage != "") {
                Tooltip(
                    tip = tipMessage,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    iconColor = toolTipIconColor,
                    offset = DpOffset(130.dp, 0.dp)
                )
            }
        }
    }
}
