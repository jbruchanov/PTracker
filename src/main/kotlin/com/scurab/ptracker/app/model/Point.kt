package com.scurab.ptracker.app.model

import androidx.compose.ui.geometry.Size

data class Point(val x: Float, val y: Float) {
    operator fun times(size: Size) = Point(x * size.width, y * size.height)
}