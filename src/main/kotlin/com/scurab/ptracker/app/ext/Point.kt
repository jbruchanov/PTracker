package com.scurab.ptracker.app.ext

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.jetbrains.skia.Point
import kotlin.math.abs

val NormalizedRange = 0f..1f

private val ONE = Point(1f, 1f)
val Point.Companion.ONE get() = com.scurab.ptracker.app.ext.ONE

fun Point.normalize(size: Size): Point {
    return if (size.isEmpty()) Point.ZERO
    else Point((x / abs(size.width)).coerceIn(NormalizedRange), (1f - (y / abs(size.height))).coerceIn(NormalizedRange))
}

fun Point.normalize(rect: Rect): Point {
    return if (rect.width == 0f || rect.height == 0f) Point.ZERO
    else Point(((x - rect.left) / abs(rect.width)).coerceIn(NormalizedRange), ((y - rect.top) / abs(rect.height)).coerceIn(NormalizedRange))
}

fun Point.transformNormToReal(size: Size): Point {
    requireNormalized()
    return Point(abs(size.width) * x, abs(size.height) * y)
}

fun Point.transformNormToViewPort(rect: Rect): Point {
    requireNormalized()
    return Point(rect.left + (x * rect.nWidth), rect.bottom + (y * rect.nHeight))
}

fun Point.requireNormalized() {
    require(x in 0f..1f) { "Invalid x:$x, should be normalized" }
    require(y in 0f..1f) { "Invalid y:$y, should be normalized" }
}
