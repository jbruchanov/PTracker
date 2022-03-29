package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

private val _One = Offset(1f, 1f)
val Offset.Companion.One get() = _One
fun Offset.translate(x: Float = 0f, y: Float = 0f) = Offset(this.x + x, this.y + y)
fun Offset.normalize(size: Size) = Offset(x / size.width, y / size.height)