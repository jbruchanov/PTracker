package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs

fun Rect.scale(scaleX: Float = 1f, scaleY: Float = 1f, pivot: Offset = Offset.Zero) =
    translate(pivot).scale(scaleX, scaleY).translate(-pivot.x, -pivot.y)

fun Rect.scale(scaleX: Float = 1f, scaleY: Float = scaleX) = Rect(
    left * scaleX, top * scaleY, right * scaleX, bottom * scaleY
)

fun Rect.toLTWH() = "L:${left.f0}, T:${top.f0}, W:${widthAbs.f0}, H:${heightAbs.f0}"
fun Rect.toLTRBWH() = "L:${left.f0}, T:${top.f0}, R:${right.f0}, B:${bottom.f0}, W:${widthAbs.f0}, H:${heightAbs.f0}"
val Rect.widthAbs get() = abs(width)
val Rect.heightAbs get() = abs(height)