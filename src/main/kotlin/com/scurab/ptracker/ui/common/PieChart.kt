package com.scurab.ptracker.ui.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.lang.Float.min

@Composable
@Preview
private fun PreviewPieChart() {
    val sampleData = listOf(
        PieChartSegment(0f, 120f, Color.Green),
        PieChartSegment(120f, 140f, Color.Red, strokeWidth = 30.dp),
        PieChartSegment(120f, 200f, Color.Blue, strokeWidth = 30.dp, radiusOffset = (-20).dp),
        PieChartSegment(320f, 180f, Color.White, strokeWidth = 5.dp, radiusOffset = (20).dp),
    )
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        PieChart(sampleData)
    }
}

data class PieChartSegment(
    val startAngle: Float,
    val sweepAngle: Float,
    val color: Color,
    val strokeWidth: Dp = 5.dp,
    val radiusOffset: Dp = 0.dp
)

@Composable
private fun PieChart(data: List<PieChartSegment>) {
    val debug = false

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (debug) {
            drawCircle(Color.Green, 50f, center = Offset.Zero)
        }

        val pieChartSize = min(size.width, size.height) / 2f
        translate(left = size.width / 2, top = size.height / 2) {
            data.forEach { (startAngle, sweepAngle, color, strokeWidth, radiusOffset) ->
                val radiusOffsetPx = radiusOffset.toPx()
                drawArc(
                    color,
                    startAngle,
                    sweepAngle,
                    useCenter = false,
                    size = Size(pieChartSize + radiusOffsetPx, pieChartSize + radiusOffsetPx),
                    style = Stroke(strokeWidth.toPx()),
                    topLeft = Offset(
                        (-pieChartSize - radiusOffsetPx) / 2f,
                        (-pieChartSize - radiusOffsetPx) / 2f
                    )
                )
            }

            if (debug) {
                drawLine(Color.Green, start = Offset(-150f, 0f), end = Offset(150f, 0f), strokeWidth = 1f)
                drawLine(Color.Green, start = Offset(0f, -150f), end = Offset(0f, 150f), strokeWidth = 1f)
            }
        }
    }
}