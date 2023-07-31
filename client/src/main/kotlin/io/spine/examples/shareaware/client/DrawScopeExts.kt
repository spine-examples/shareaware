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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.jetbrains.skia.Point

/**
 * Draws an Y axis inside the drawing area.
 *
 * @param color the color of the axis line
 * @param axisWidth the width of the axis line
 */
public fun DrawScope.drawYAxis(
    color: Color,
    axisWidth: Float = 2f
) {
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(0f, size.height),
        strokeWidth = axisWidth
    )
}

/**
 * Draws an X axis inside the drawing area.
 *
 * @param color the color of the axis line
 * @param axisWidth the width of the axis line
 */
public fun DrawScope.drawXAxis(
    color: Color,
    axisWidth: Float = 2f
) {
    drawLine(
        color = color,
        start = Offset(0f, size.height - 0f),
        end = Offset(size.width, size.height - 0f),
        strokeWidth = axisWidth
    )
}

/**
 * Maps a list of points from their original range to pixel coordinates within the drawing area.
 *
 * @param points the list of points to map
 */
public fun DrawScope.mapToPixelPoints(points: List<Point>): List<Point> {
    val minXValue = points.minOf { it.x }
    val maxXValue = points.maxOf { it.x }
    val minYValue = points.minOf { it.y }
    val maxYValue = points.maxOf { it.y }
    return points.map {
        val x = it.x.mapToDifferentRange(
            inMin = minXValue,
            inMax = maxXValue,
            outMin = 0f,
            outMax = size.width
        )
        val y = it.y.mapToDifferentRange(
            inMin = minYValue,
            inMax = maxYValue,
            outMin = size.height,
            outMax = 0f
        )
        Point(x, y)
    }
}

/**
 * Remaps a value from one range to another.
 *
 * @param inMin The minimum value of the original range.
 * @param inMax The maximum value of the original range.
 * @param outMin The minimum value of the target range.
 * @param outMax The maximum value of the target range.
 */
private fun Float.mapToDifferentRange(
    inMin: Float,
    inMax: Float,
    outMin: Float,
    outMax: Float
): Float = (this - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
