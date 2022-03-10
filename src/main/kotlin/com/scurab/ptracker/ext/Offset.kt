package com.scurab.ptracker.ext

import androidx.compose.ui.geometry.Offset


fun Offset.translate(x: Float = 0f, y: Float = 0f) = Offset(this.x + x, this.y + y)