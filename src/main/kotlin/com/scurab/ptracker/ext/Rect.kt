package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.max
import kotlin.math.min

fun Rect.scale(scaleX: Float = 1f, scaleY: Float = 1f, pivot: Offset = Offset.Zero): Rect {
    return translate(pivot).scale(scaleX, scaleY).translate(-pivot)
}


fun Rect.scale(scaleX: Float = 1f, scaleY: Float = scaleX) = Rect(
    left * scaleX, top * scaleY, right * scaleX, bottom * scaleY
)

fun Rect.toLTWH() = "L:${left.f0}, T:${top.f0}, W:${nWidth.f0}, H:${nHeight.f0}"
fun Rect.toLTRBWH() = "L:${left.f0}, T:${top.f0}, R:${right.f0}, B:${bottom.f0}, W:${nWidth.f0}, H:${nHeight.f0}"
val Rect.nWidth get() = max(right, left) - min(right, left)
val Rect.nHeight: Float get() = max(top, bottom) - min(top, bottom)