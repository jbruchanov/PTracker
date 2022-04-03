package com.scurab.ptracker.app.ext

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.scurab.ptracker.app.model.Point

fun List<Point>.toAbsoluteCoordinatesPath(size: Size): Path {
    val data = this
    return Path().apply {
        firstOrNull()
            ?.let { it * size }
            ?.let { moveTo(it.x, it.y) }

        (1 until (data.size)).forEach { index ->
            val p0 = data[index - 1] * size
            val p1 = data[index] * size
            val c1x = (p0.x + p1.x) / 2
            val c1y = p0.y
            val c2x = (p0.x + p1.x) / 2
            val c2y = p1.y
            cubicTo(c1x, c1y, c2x, c2y, p1.x, p1.y)
        }
    }
}