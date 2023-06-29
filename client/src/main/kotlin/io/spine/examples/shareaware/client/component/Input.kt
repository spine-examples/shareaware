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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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

/**
 * The input component that supports displaying a tip.
 *
 * @param value the input text to be shown in the text field
 * @param onChange the callback that is triggered when the input's value changes
 * @param placeholder the label to be displayed inside the input container
 * @param isError indicates if the input's current value is in error
 * @param tipMessage message to be displayed in the tooltip
 * @param containerColor the color used for the background of this input
 * @param leadingIcon the optional leading icon to be displayed at the beginning of the input field container
 * @param contentPadding the spacing values to apply internally between the input container and the content
 */
@Composable
public fun Input(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    tipMessage: String = "",
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    leadingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp, 8.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Unspecified
    BasicTextField(
        value = value,
        onValueChange = onChange,
        textStyle = MaterialTheme.typography.bodySmall,
        interactionSource = interactionSource,
        maxLines = 1
    ) { innerTextField ->
        InnerBox(
            borderColor = borderColor,
            containerColor = containerColor,
            contentPadding = contentPadding,
        ) {
            showLeadingIcon(leadingIcon != null) { leadingIcon!!() }
            InputContainer(
                value = value,
                placeholder = placeholder,
                textField = { innerTextField() },
                modifier = { this.weight(1f) }
            )
            showTooltip(tipMessage, isError, Modifier.align(Alignment.CenterVertically))
        }
    }
}

/**
 * Inner box for all content of the `Input`.
 */
@Composable
private fun InnerBox(
    borderColor: Color,
    containerColor: Color,
    contentPadding: PaddingValues,
    content: @Composable RowScope.() -> Unit
) {
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
                paddingValues = contentPadding
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

/**
 * Container for the text field.
 */
@Composable
private fun InputContainer(
    value: String,
    placeholder: String,
    textField: @Composable () -> Unit,
    modifier: Modifier.() -> Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .modifier(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Placeholder(
            isShown = value.isEmpty(),
            value = placeholder
        )
        textField()
    }
}

/**
 * Displays the leading icon of the `Input`.
 */
@Composable
private fun showLeadingIcon(isShown: Boolean, icon: @Composable () -> Unit) {
    if (isShown) {
        icon()
    }
}

/**
 * Displays the tooltip of the `Input`.
 */
@Composable
private fun showTooltip(
    message: String,
    isError: Boolean,
    modifier: Modifier
) {
    val toolTipIconColor = if (isError) MaterialTheme.colorScheme.error else
        MaterialTheme.colorScheme.onSecondary
    if (message != "") {
        Tooltip(
            tip = message,
            modifier = modifier,
            iconColor = toolTipIconColor,
            offset = DpOffset(130.dp, 0.dp)
        )
    }
}

/**
 * The placeholder to be shown inside the text field.
 */
@Composable
private fun Placeholder(
    isShown: Boolean,
    value: String
) {
    if (isShown) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}
