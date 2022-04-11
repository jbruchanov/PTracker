package com.scurab.ptracker.ui.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.toAbsoluteCoordinatesPath
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.model.Point
import kotlin.random.Random

@Composable
@Preview
private fun PreviewLineChart() {
    val steps = 40
    val random = Random(10)
    val sampleData = (0..steps).map {
        Point(it.toFloat() / steps, 0.5f + (random.nextInt(-4, 4) / 10f))
    }
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        LineChart(
            sampleData,
            style = Stroke(5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 10.dp.toPx()))),
            strokeColor = Color.White,
            fillingGradientColors = listOf(Color.White, Color.Black)
        )
    }
}

@Composable
fun LineChart(
    relativePoints: List<Point>,
    style: DrawStyle = Stroke(5.dp.toPx()),
    strokeColor: Color = Color.White,
    fillingGradientColors: List<Color>? = null
) {
    val debug = false
    val density = LocalDensity.current.density
    BoxWithConstraints {
        val canvasSize = remember(maxWidth, maxHeight) { Size(maxWidth.toPx(density), maxHeight.toPx(density)) }
        val lienPath = remember(canvasSize, relativePoints) { relativePoints.toAbsoluteCoordinatesPath(canvasSize) }
        val fillPath = remember(canvasSize, fillingGradientColors, relativePoints) {
            Path().apply {
                if (fillingGradientColors != null) {
                    addPath(lienPath)
                    lineTo(canvasSize.width, canvasSize.height)
                    lineTo(0f, canvasSize.height)
                    close()
                    fillType = PathFillType.EvenOdd
                }
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (debug) {
                drawCircle(Color.Green, 50f, center = Offset.Zero)
            }
            drawPath(path = lienPath, color = strokeColor, style = style)
            if (fillingGradientColors != null && fillingGradientColors.isNotEmpty()) {
                val brush = Brush.linearGradient(fillingGradientColors, end = Offset(0f, size.height))
                drawPath(path = fillPath, brush = brush)
            }

            if (debug) {
                val points = relativePoints.map { Offset(it.x * size.width, it.y * size.height) }
                drawPoints(points, PointMode.Points, Color.Magenta, strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                translate(left = size.width / 2, top = size.height / 2) {
                    drawLine(Color.Green, start = Offset(-150f, 0f), end = Offset(150f, 0f), strokeWidth = 1f)
                    drawLine(Color.Green, start = Offset(0f, -150f), end = Offset(0f, 150f), strokeWidth = 1f)
                }
            }
        }
    }
}