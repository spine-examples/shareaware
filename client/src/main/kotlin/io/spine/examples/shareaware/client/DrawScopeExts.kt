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
 * Scales a list of points to fit the given size of the drawing area.
 *
 * @param points the list of points to scale
 */
public fun DrawScope.scalePoints(points: List<Point>): List<Point> {
    val minXValue = points.minOf { it.x }
    val maxXValue = points.maxOf { it.x }
    val minYValue = points.minOf { it.y }
    val maxYValue = points.maxOf { it.y }
    return points.map {
        val x = it.x.mapToDifferentRange(
            originalMin = minXValue,
            originalMax = maxXValue,
            targetMin = 0f,
            targetMax = size.width
        )
        val y = it.y.mapToDifferentRange(
            originalMin = minYValue,
            originalMax = maxYValue,
            targetMin = size.height,
            targetMax = 0f
        )
        Point(x, y)
    }
}

/**
 * Remaps a value from one range to another.
 *
 * @param originalMin the minimum value of the original range
 * @param originalMax the maximum value of the original range
 * @param targetMin the minimum value of the target range
 * @param targetMax the maximum value of the target range
 */
private fun Float.mapToDifferentRange(
    originalMin: Float,
    originalMax: Float,
    targetMin: Float,
    targetMax: Float
): Float = (this - originalMin) * (targetMax - targetMin) / (originalMax - originalMin) + targetMin
