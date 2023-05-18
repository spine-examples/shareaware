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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * UI theme for the application, based upon Material 3 guidelines.
 *
 * Provides styles (color scheme, typography, shapes)
 * that standard components will use as default.
 * Also, it can be used to style any component to match the application theme.
 *
 * @see MaterialTheme
 */
@Composable
public fun ShareAwareTheme(content: @Composable () -> Unit) {
    MaterialTheme (
        colorScheme = lightColors,
        typography = typography,
        content = content
    )
}

/**
 * The light colors scheme of the application.
 */
private val lightColors: ColorScheme = lightColorScheme(
    primary = Colors.Blue30,
    onPrimary = Colors.White,
    secondary = Colors.Blue90,
    onSecondary = Color.Black,
    onSecondaryContainer = Color.Black,
    surface = Colors.Beige40,
    onSurface = Color.Black,
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = Color.Black,
    surfaceTint = Colors.Blue80,
    background = Colors.Beige90,
    onBackground = Color.Black,
    outline = Colors.Blue30
)

/**
 * Text styles of the application.
 */
private val typography: Typography = Typography(
    bodySmall = TextStyle(
        fontSize = 12.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 17.sp
    ),
    labelMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontSize = 30.sp
    )
)

private object Colors {
    val Blue90: Color = Color(0xffb0daff)
    val Blue30: Color = Color(0xff1d267d)
    val Blue80: Color = Color(0xffacb3ec)
    val White: Color = Color(0xfff8f5e4)
    val Beige90: Color = Color(0xfff9f5eb)
    val Beige40: Color = Color(0xffe3dccf)
}