package com.scurab.ptracker.app.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

fun size(width: Int, height: Int) = Size(width.toFloat(), height.toFloat())
fun Size.toOffset() = Offset(width, height)

operator fun Size.times(offset: Offset) = Size(width * offset.x, height * offset.y)