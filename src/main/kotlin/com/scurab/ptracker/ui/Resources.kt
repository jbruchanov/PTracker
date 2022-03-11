package com.scurab.ptracker.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Typeface

object PriceDashboardColor {
    val Background = Color.Black
    val OnBackground = Color.White
    val GridLine = Color.DarkGray
    val BackgroundAxis = Color.Black.copy(alpha = 0.75f)
    val MouseCross = Color.White
    val Debug = Color.Magenta

    val CandleRed = Color(0xFFEF5350)
    val CandleGreen = Color(0xFF26A69A)
}

object PriceDashboardSizes {
    val GridLineStrokeWidth = 1.dp
    val SpikeLineStrokeWidth = 1.dp
    val MouseCrossStrokeWidth = 1.dp
    val AxisPadding = 2.dp
    const val PriceItemWidth = 10f
}

object TextRendering {
    val font = Font(Typeface.makeFromName("monospace", FontStyle.NORMAL))
    val fontAxis = Font(Typeface.makeFromName("monospace", FontStyle.NORMAL), 16.sp.value)
    val paint = Paint().apply { this.color = PriceDashboardColor.OnBackground.toArgb(); this.isAntiAlias = true }
    val axisXStep = 5
    val axisYStep = 5
}