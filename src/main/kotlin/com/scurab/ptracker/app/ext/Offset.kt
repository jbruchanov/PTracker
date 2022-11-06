package com.scurab.ptracker.app.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.unit.IntSize

private val _One = Offset(1f, 1f)
val Offset.Companion.One get() = _One
fun Offset.translate(x: Float = 0f, y: Float = 0f) = Offset(this.x + x, this.y + y)
fun Offset.normalize(size: Size) = Offset(x / size.width, y / size.height)
operator fun Offset.times(size: Size): Offset = takeIf { it.isSpecified }?.let { Offset(it.x * size.width, it.y * size.height) } ?: Offset.Zero
operator fun Offset.times(size: IntSize): Offset = takeIf { it.isSpecified }?.let { Offset(it.x * size.width, it.y * size.height) } ?: Offset.Zero
operator fun Offset.div(size: Size): Offset = Offset(this.x / size.width, this.y / size.height)
operator fun Offset.div(size: IntSize): Offset = Offset(this.x / size.width, this.y / size.height)
