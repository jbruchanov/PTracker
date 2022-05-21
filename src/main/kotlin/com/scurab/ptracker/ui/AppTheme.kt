package com.scurab.ptracker.ui

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scurab.ptracker.App
import com.scurab.ptracker.app.ext.FloatRange
import com.scurab.ptracker.app.ext.scaled
import com.scurab.ptracker.app.ext.toLabelPrice
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.icons.TriangleDown
import com.scurab.ptracker.app.icons.TriangleUp
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.component.compose.StateContainer
import com.scurab.ptracker.ui.model.IconColor
import org.jetbrains.skia.Data
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
            LocalScrollbarStyle provides defaultScrollbarStyle().copy(
                hoverColor = AppTheme.Colors.Secondary,
                unhoverColor = AppTheme.Colors.SecondaryVariant,
            )
        ) {
            val contentPadding = AppSizes.current.Space05
            Surface(
                modifier = Modifier.fillMaxSize()
                    .background(AppColors.current.WindowEdge)
                    .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
                    .background(AppColors.current.BackgroundContent)
            ) {
                block()
            }
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
        val SecondaryVariant = Color(0xFFFF7F00).copy(alpha = .75f)
        val BackgroundContent = Color(0xFF2B2B2B)
        val BackgroundAssetIcon = Color.White.copy(.85f)
        val ToDo = Color.Magenta
        val OnBackground = Color.White
        val OnBackgroundVariant = Color.White.copy(.1f).compositeOver(Primary)
        val Content = StateContainer(default = OnBackground, selected = Secondary)
        val ButtonBackground = StateContainer(default = Primary)
        val WindowEdge = Color.White
        val RowBackground = StateContainer(
            default = Primary.copy(alpha = 0.1f),
            default2 = Primary.copy(alpha = 0.15f),
            selected = Secondary.copy(alpha = .15f).compositeOver(Primary.copy(alpha = 0.1f))
        )
        val FooterBackground = RowBackground.copy(default = Color.Transparent, default2 = Color.Transparent)
        val Red = Color(0xFFEF5350)
        val Green = Color(0xFF26A69A)
        val RedGreen = StateContainer(Red, default2 = Green)
        val RedGreenWhite = StateContainer(default = Red, default2 = Green, disabled = OnBackground)

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
        val RoundedCornersSize2 = RoundedCornerShape(Sizes.Space2)
        val RoundedCornersSize4 = RoundedCornerShape(Sizes.Space4)
    }

    object Sizes {
        private val MinClickableSize = 40.dp
        private val ClickableSize = 48.dp
        val IconButtonPadding = 8.dp
        val IconTransactionType = 16.dp
        val StatsIconSize = 42.dp

        val Space025 = 1.dp
        val Space05 = 2.dp
        val Space = 4.dp
        val Space2 = 8.dp
        val Space4 = 16.dp
        val Space6 = 24.dp
        val Space8 = 32.dp

        val Padding05 = 4.dp
        val Padding = 8.dp

        val ThinLine = 1.dp
        val ThickLine = 2.dp

        @Composable
        fun minClickableSize() = MinClickableSize.scaled()

        @Composable
        fun clickableSize() = ClickableSize.scaled()
    }

    object Values {
        val DividerDefaultAlpha = 0.12f
    }

    object MouseCursors {
        val PointerIconCross = PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR))
        val PointerIconResizeVertically = PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR))
        val PointerIconMove = PointerIcon(Cursor(Cursor.MOVE_CURSOR))
    }

    object TextRendering {
        val small = 12.sp
        val xlarge = 24.sp
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

        fun flagFont(): Font {
            val bytes = App::class.java.classLoader.getResourceAsStream("babelstoneflags.ttf").readBytes()
            return Font(Typeface.makeFromData(Data.makeFromBytes(bytes)))
        }
    }

    object DashboardColors {
        val OnBackground = Colors.OnBackground
        val GridLine = Color.DarkGray
        val BackgroundAxisEdge = Color.White.copy(alpha = 0.75f)
        val BackgroundAxis = Color(0xFF202020).copy(alpha = 0.75f)
        val BackgroundPriceBubble = Color(0xFF404040)
        val MouseCross = Color.White
        val Candle = Colors.RedGreen
    }

    object DashboardSizes {
        //no dp, this basically means 10px per a price candle, scale handles proper scaling
        const val PriceItemWidth = 10f

        val GridLineStrokeWidth = Sizes.Space025
        val SpikeLineStrokeWidth = Sizes.Space025
        val MouseCrossStrokeWidth = Sizes.Space025
        val VerticalAxisHorizontalPadding = 8.dp

        //depends on fontAxis size
        val BottomAxisContentMinHeight = 30.dp
        val PriceSelectedDayDetail = 12.sp
    }

    object TransactionIcons {
        private val ColorGreen = Color(0xFF00FF00)
        private val ColorRed = Color(0xFFFF0000)
        private val ColorWhite = Color(0xFFFFFFFFF)
        private val ColorGray = Color.LightGray
        private val ColorOrange = Color(0xFFFFC100)
        private val ColorBlack = Color.Black
        val IconsMap = mapOf(
            Transaction.TypeDeposit to IconColor(0, Icons.Default.ArrowDownward, ColorWhite, Offset(0f, 4f)),
            Transaction.TypeWithdrawal to IconColor(1, Icons.Default.ArrowUpward, ColorWhite, Offset(0f, -4f)),
            Transaction.TypeAirdrop to IconColor(2, Icons.Outlined.WbCloudy, ColorGreen),
            Transaction.TypeMining to IconColor(3, Icons.Outlined.Star, ColorRed),
            Transaction.TypeStaking to IconColor(4, Icons.Outlined.Star, ColorOrange),
            Transaction.TypeInterest to IconColor(5, Icons.Outlined.Star, ColorGray),
            Transaction.TypeDividend to IconColor(6, Icons.Outlined.Star, ColorGray),
            Transaction.TypeIncome to IconColor(7, Icons.Outlined.Star, ColorWhite),
            Transaction.TypeGiftReceived to IconColor(8, Icons.Outlined.FavoriteBorder, ColorGreen),
            Transaction.TypeGiftSent to IconColor(9, Icons.Outlined.FavoriteBorder, ColorRed),
            Transaction.TypeCharitySent to IconColor(10, Icons.Outlined.FavoriteBorder, ColorOrange),
            Transaction.TypeGiftSpouse to IconColor(11, Icons.Filled.Favorite, ColorWhite),
            Transaction.TypeLost to IconColor(12, Icons.Default.Clear, ColorBlack),
            Transaction._TypeCryptoDeposit to IconColor(91, Icons.Default.ArrowDownward, ColorGreen, Offset(0f, 4f)),
            Transaction._TypeCryptoWithdrawal to IconColor(92, Icons.Default.ArrowUpward, ColorRed, Offset(0f, -4f)),
            Transaction._TypeTradeIn to IconColor(98, Icons.Default.TriangleDown, ColorGreen, Offset(0f, 4f), candleScale = IconColor.CandleScaleTrade),
            Transaction._TypeTradeOut to IconColor(99, Icons.Default.TriangleUp, ColorRed, Offset(0f, -4f), candleScale = IconColor.CandleScaleTrade),
            Transaction._TypeCryptoExchange to IconColor(81, Icons.Default.SwapHoriz, ColorGreen, Offset(0f, 0f)),
            Transaction._TypeFiatExchange to IconColor(88, Icons.Default.SwapHoriz, ColorWhite, Offset(0f, 0f)),
        )
    }

    object TextStyles {
        val Header = TextStyle(color = Colors.OnBackground, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
        val Normal = TextStyle(color = Colors.OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Normal)
        val Small = TextStyle(color = Colors.OnBackground, fontSize = 12.sp, fontWeight = FontWeight.Normal)
        val Tiny = TextStyle(color = Colors.OnBackground, fontSize = 10.sp, fontWeight = FontWeight.Normal)
        val NormalMonospace = TextStyle(color = Colors.OnBackground, fontSize = 14.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
        val SmallMonospace = TextStyle(color = Colors.OnBackground, fontSize = 12.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
        val TinyMonospace = TextStyle(color = Colors.OnBackground, fontSize = 10.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
        val Monospace = TextStyle(color = Colors.OnBackground, fontSize = 16.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)

        val TransactionPrimary = TextStyle(color = Colors.OnBackground, fontSize = 13.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
        val TransactionPrimaryVariant = TextStyle(color = Colors.Secondary, fontSize = 13.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
        val TransactionSecondary = TextStyle(color = Colors.Secondary, fontSize = 12.sp, fontWeight = FontWeight.Normal, fontFamily = FontFamily.Monospace)
        val TransactionDetail = TextStyle(color = Colors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Light)
        val TransactionMoneyAnnotation = TextStyle(color = Colors.OnBackgroundVariant, fontSize = 11.sp, fontWeight = FontWeight.Normal)
    }

    object SpanStyles {
        fun small(color: Color = Colors.OnBackground) = SpanStyle(color = color, fontSize = 12.sp, fontWeight = FontWeight.Normal)
        fun tiny(color: Color = Colors.OnBackground) = SpanStyle(color = color, fontSize = 10.sp, fontWeight = FontWeight.Normal)
    }
}

val RandomColors = listOf(Color.Red, Color.Green, Color.Yellow, Color.LightGray, Color.Cyan)
val CoinColors = mapOf(
    "ADA" to Color(0xFF0033AD),
    "AAVE" to Color(0xFF40ACC1),
    "BTC" to Color(0xFFF7931A),
    "ETH" to Color(0xFF62688F),
    "DOT" to Color(0xFFE6007A),
    "LTC" to Color(0xFF345D9D),
    "LUNA" to Color(0xFFFFD952),
    "SOL" to Color(0xFFB14AED),
)