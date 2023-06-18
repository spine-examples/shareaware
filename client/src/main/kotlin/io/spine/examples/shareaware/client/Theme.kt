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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
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
    secondary = Colors.Grey,
    onSecondary = Colors.DarkGrey,
    onSecondaryContainer = Color.Black,
    tertiary = Colors.White10,
    surface = Colors.White30,
    onSurface = Color.Black,
    surfaceVariant = Color.Transparent,
    onSurfaceVariant = Color.Black,
    surfaceTint = Colors.Blue80,
    background = Colors.White,
    onBackground = Color.Black,
    outline = Colors.Blue30,
    error = Colors.Red,
    scrim = Colors.Scrim
)

/**
 * [San Francisco font](https://developer.apple.com/fonts/#:~:text=across%20multiple%20interfaces.-,San%20Francisco,-San%20Francisco%20is)
 */
private val sanFrancisco = FontFamily(
    Font(
        "font/SFUIDisplay-Bold.ttf",
        FontWeight.Bold,
        FontStyle.Normal
    ),
    Font(
        "font/SFUIDisplay-Semibold.ttf",
        FontWeight.Medium,
        FontStyle.Normal
    ),
    Font(
        "font/SFUIText-Regular.ttf",
        FontWeight.Normal,
        FontStyle.Normal
    )
)

/**
 * Text styles of the application.
 */
private val typography: Typography = Typography(
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontFamily = sanFrancisco
    ),
    headlineSmall = TextStyle(
        fontSize = 12.sp,
        fontFamily = sanFrancisco
    ),
    headlineMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.W400,
        fontFamily = sanFrancisco
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.5.sp,
        fontFamily = sanFrancisco
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontFamily = sanFrancisco
    ),
    labelSmall = TextStyle(
        fontFamily = sanFrancisco,
        fontSize = 17.sp,
        fontWeight = FontWeight.W500
    ),
    labelMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.W500,
        letterSpacing = 0.5.sp,
        fontFamily = sanFrancisco
    ),
    labelLarge = TextStyle(
        fontSize = 47.sp,
        fontFamily = sanFrancisco,
    )
)

private object Colors {
    val Blue30 = Color(0xff2094fa)
    val Blue80 = Color(0xffacb3ec)
    val White = Color(0xfff5f5f5)
    val White10 = Color(0xffdad8de)
    val White30 = Color(0xe6e6e6)
    val Grey = Color(0xffc6c4c9)
    val DarkGrey = Color(0xff5b595f)
    val Red = Color(0xffff3b30)
    val Scrim = Color.Black.copy(alpha = 0.5f)
}
