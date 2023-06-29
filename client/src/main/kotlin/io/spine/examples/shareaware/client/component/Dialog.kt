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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import io.spine.examples.shareaware.client.PrimaryButton
import java.awt.event.KeyEvent

/**
 * Dialog window component.
 *
 * @param onCancel callback that will be called when the user clicks on `Cancel` button
 * @param onConfirm callback that will be called when the user clicks on `Confirm` button
 * @param title the title of the dialog which should specify the purpose of the dialog
 * @param modifier the `Modifier` to be applied to this dialog
 * @param inputs the list of composable that should represent the list of input fields
 */
@Composable
public fun Dialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    vararg inputs: @Composable () -> Unit
) {
    Popup(
        onDismissRequest = onCancel,
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset.Zero
        },
        focusable = true,
        onKeyEvent = {
            if (it.type == KeyEventType.KeyDown && it.awtEventOrNull?.keyCode == KeyEvent.VK_ESCAPE) {
                onCancel()
                true
            } else {
                false
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
                .pointerInput(onCancel) {
                    detectTapGestures(onPress = { onCancel() })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .shadow(elevation = 24.dp, shape = MaterialTheme.shapes.large)
                    .pointerInput(onCancel) {
                        detectTapGestures(onPress = {})
                    }
                    .background(MaterialTheme.colorScheme.background)
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainSection(
                    title = title,
                    inputs = inputs
                )
                ControlSection(onCancel, onConfirm)
            }
        }
    }
}

/**
 * The main section of the dialog window.
 *
 * @param title the title of the window
 * @param inputs the list of inputs that will be located one below the other
 */
@Composable
private fun MainSection(
    title: String,
    vararg inputs: @Composable () -> Unit
) {
    Text(
        title,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(20.dp))
    inputs.forEachIndexed { index, input ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            input()
        }
        if (index != inputs.size - 1) {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

/**
 * The control section of the dialog window.
 *
 * @param onCancel the callback function that will be triggered when the "Cancel" button pressed
 * @param onConfirm the callback function that will be triggered when the "Confirm" button pressed
 */
@Composable
private fun ControlSection(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        PrimaryButton(
            onCancel,
            "Cancel",
            modifier = Modifier
                .width(110.dp)
                .height(30.dp),
            labelStyle = MaterialTheme.typography.bodySmall,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(2.dp),
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(5.dp))
        PrimaryButton(
            onConfirm,
            "Confirm",
            modifier = Modifier
                .width(110.dp)
                .height(30.dp),
            labelStyle = MaterialTheme.typography.bodySmall,
            shape = MaterialTheme.shapes.small,
            contentPadding = PaddingValues(2.dp)
        )
    }
}
