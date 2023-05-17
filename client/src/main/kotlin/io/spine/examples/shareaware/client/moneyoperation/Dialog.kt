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

package io.spine.examples.shareaware.client.moneyoperation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.spine.examples.shareaware.client.PrimaryButton

/**
 * Dialog window component.
 *
 * @param onCancel callback that will be called when the user clicks on `Cancel` button
 * @param onConfirm callback that will be called when the user clicks on `Confirm` button
 * @param title the title of the dialog which should specify the purpose of the dialog
 * @param inputs the list of composable that should represent the list of input fields
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
public fun Dialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    vararg inputs: @Composable () -> Unit
) {
    AlertDialog(
        title = {
            MainSection(
                title = title,
                inputs = inputs
            )
        },
        buttons = {
            ControlSection(onCancel, onConfirm)
        },
        onDismissRequest = onCancel,
        modifier = Modifier
            .width(400.dp)
            .height(250.dp),
        shape = MaterialTheme.shapes.large,
        backgroundColor = MaterialTheme.colorScheme.background
    )
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
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))
        inputs.forEachIndexed { index, input ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                input()
            }
            if (index != inputs.size - 1) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * The control section of the dialog window.
 */
@Composable
private fun ControlSection(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(end = 20.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PrimaryButton(
            onCancel,
            "Cancel",
            modifier = Modifier
                .width(110.dp)
                .height(40.dp),
            labelStyle = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(5.dp))
        PrimaryButton(
            onConfirm,
            "Confirm",
            modifier = Modifier
                .width(110.dp)
                .height(40.dp),
            labelStyle = MaterialTheme.typography.bodyMedium
        )
    }
}
