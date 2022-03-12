package com.scurab.ptracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scurab.ptracker.ext.toPx
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Typeface
import java.awt.Cursor
import java.time.format.DateTimeFormatter

object PriceDashboardColor {
    val Background = Color(0xFF151924)
    val OnBackground = Color.White
    val GridLine = Color.DarkGray
    val BackgroundAxisEdge = Color.White.copy(alpha = 0.75f)
    val BackgroundAxis = Color(0xFF202020).copy(alpha = 0.75f)
    val BackgroundPriceBubble = Color(0xFF404040)
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
    val PriceItemWidth = 10f
    //depends on fontAxis size
    val VerticalPriceBarWidth = 60.dp
    val BottomAxisContentMinHeight = 30.dp
}

object TextRendering {
    val font = Font(Typeface.makeFromName("monospace", FontStyle.NORMAL))
    val fontAxis = Font(Typeface.makeFromName("verdana", FontStyle.NORMAL))
    val fontLabels = Font(Typeface.makeFromName("verdana", FontStyle.NORMAL))
    val paint = Paint().apply { this.color = PriceDashboardColor.OnBackground.toArgb(); this.isAntiAlias = true }
    val axisXStep = 5

    @Composable
    fun init() {
        font.size = 14.sp.toPx(LocalDensity)
        fontAxis.size = 14.sp.toPx(LocalDensity)
        fontLabels.size = 14.sp.toPx(LocalDensity)
    }
}

object DateFormats {
    val fullDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val year = DateTimeFormatter.ofPattern("yyyy")
    val monthMid = DateTimeFormatter.ofPattern("MMM")
    val monthYear = DateTimeFormatter.ofPattern("MM/yyyy")
    val dayNumber = DateTimeFormatter.ofPattern("d")
}

object MouseCursors {
    val PointerIconCross = PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR))
    val PointerIconResizeVertically = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
}