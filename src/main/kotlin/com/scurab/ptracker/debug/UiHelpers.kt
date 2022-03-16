package com.scurab.ptracker.debug

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

@Composable
fun TestWindow() {
    Canvas(modifier = Modifier.size(500.dp, 500.dp)) {
        scale(2f, 1f, pivot = Offset(size.width, size.height / 2)) {
            drawRect(Color.Black, size = size)
            DebugGrid()
        }
    }
}

fun DrawScope.DebugGrid() {
    val canvasSize = size
    translate(size.width / 2, size.height / 2) {
        val size = 100f
        drawLine(Color.Magenta, start = Offset(0f, -size), end = Offset(0f, size))
        drawLine(Color.Magenta, start = Offset(-size, 0f), end = Offset(size, 0f))

        drawRect(Color.Magenta, topLeft = Offset(-size / 2, -size / 2), Size(size, size), style = Stroke(width = 1.dp.toPx()))
        rotate(45f, pivot = Offset.Zero) {
            drawRect(Color.Magenta, topLeft = Offset(-size / 2, -size / 2), Size(size, size), style = Stroke(width = 1.dp.toPx()))
        }
    }
}