package com.scurab.ptracker.ui

import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scurab.ptracker.component.compose.StateColor
import com.scurab.ptracker.ext.FloatRange
import com.scurab.ptracker.ext.toLabelPrice
import com.scurab.ptracker.ext.toPx
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface
import java.awt.Cursor

@Composable
fun AppTheme(block: @Composable () -> Unit) {
    AppTheme.TextRendering.init()
    MaterialTheme(
        colors = AppTheme.MaterialColors
    ) {
        CompositionLocalProvider(
            LocalContentColor provides AppTheme.MaterialColors.onBackground,
            AppColors provides AppTheme.Colors,
            AppShapes provides AppTheme.Shapes,
            AppSizes provides AppTheme.Sizes,
        ) {
            block()
        }
    }
}

val AppColors = compositionLocalOf { AppTheme.Colors }
val AppShapes = compositionLocalOf { AppTheme.Shapes }
val AppSizes = compositionLocalOf { AppTheme.Sizes }

object AppTheme {
    val MaterialColors = Colors.DarkMaterial

    object Colors {
        val Primary = Color(0xFF546E7A)
        val PrimaryVariant = Color(0xFF546E7A).copy(alpha = .75f)
        val Secondary = Color(0xFFFF7F00)
        val BackgroundContent = Color(0xFF2B2B2B)
        val ToDo = Color.Magenta
        val OnBackground = Color.White
        val ContentColor = StateColor(default = OnBackground, selected = Secondary)
        val ToggleButtonBackground = StateColor(default = Primary)
        val WindowEdge = Color.White

        val DarkMaterial = darkColors(
            primary = Primary,
            primaryVariant = PrimaryVariant,
            secondary = Secondary,
            secondaryVariant = ToDo,
            background = BackgroundContent,
            surface = BackgroundContent,
            onBackground = OnBackground,
            onPrimary = OnBackground,
            error = ToDo,
            onSecondary = ToDo,
            onSurface = Primary,
            onError = ToDo
        )
    }

    object Shapes {

    }

    object Sizes {
        val MinClickableSize = 40.dp
        val ClickableSize = 48.dp
        val IconButtonPadding = 8.dp

        val Hairline = 1.dp
        val Space05 = 2.dp
        val Space = 4.dp
        val Space2 = 8.dp
        val Space4 = 16.dp
    }

    object Values {
        val DividerDefaultAlpha = 0.12f
    }

    object MouseCursors {
        val PointerIconCross = PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR))
        val PointerIconResizeVertically = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
    }

    object TextRendering {
        val small = 12.sp
        val font = Font(Typeface.makeFromName("monospace", FontStyle.NORMAL))
        val fontAxis = Font(Typeface.makeFromName("verdana", FontStyle.NORMAL))
        val fontLabels = Font(Typeface.makeFromName("verdana", FontStyle.NORMAL))
        val paint = Paint().apply { this.color = DashboardColors.OnBackground.toArgb(); this.isAntiAlias = true }

        @Composable
        fun init() {
            font.size = 14.sp.toPx(LocalDensity)
            fontAxis.size = 12.sp.toPx(LocalDensity)
            fontLabels.size = 14.sp.toPx(LocalDensity)
        }

        fun measureAxisWidth(range: FloatRange): Float {
            val min = range.start.toLabelPrice(range)
            val max = range.endInclusive.toLabelPrice(range)
            val v = max.takeIf { max.length > min.length } ?: min
            return TextLine.make(v, fontAxis).width
        }
    }

    object DashboardColors {
        val OnBackground = Colors.OnBackground
        val GridLine = Color.DarkGray
        val BackgroundAxisEdge = Color.White.copy(alpha = 0.75f)
        val BackgroundAxis = Color(0xFF202020).copy(alpha = 0.75f)
        val BackgroundPriceBubble = Color(0xFF404040)
        val MouseCross = Color.White
        val CandleRed = Color(0xFFEF5350)
        val CandleGreen = Color(0xFF26A69A)
        val CandleTransaction = Colors.Primary
    }

    object DashboardSizes {
        //no dp, this basically means 10px per a price candle, scale handles proper scaling
        const val PriceItemWidth = 10f

        val GridLineStrokeWidth = Sizes.Hairline
        val SpikeLineStrokeWidth = Sizes.Hairline
        val MouseCrossStrokeWidth = Sizes.Hairline
        val VerticalAxisHorizontalPadding = 8.dp

        //depends on fontAxis size
        val VerticalPriceBarWidth = 60.dp
        val BottomAxisContentMinHeight = 30.dp

        val PriceSelectedDayDetail = 12.sp

        val TransctionIconScale = Offset(0.5f, 0.5f)
        val TransctionTradeIconScale = Offset(0.75f, 0.75f)
    }
}

