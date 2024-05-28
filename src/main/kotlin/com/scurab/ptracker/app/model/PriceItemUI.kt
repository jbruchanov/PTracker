package com.scurab.ptracker.app.model

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.AppTheme.DashboardColors
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.datetime.toJavaLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode

data class PriceItemUI(
    val index: Int,
    override val asset: Asset,
    val item: IPriceItem
) : IPriceItem by item, WithCache by MapCache(), MarketPrice {
    private val rectHeight = (open - close).abs().toFloat()
    val rectOffsetY = open.min(close).toFloat()
    val color = DashboardColors.Candle.default2If(open < close)
    val rectSize = Size(AppTheme.DashboardSizes.PriceItemWidth, rectHeight)
    val centerY = (open + close).toFloat() / 2f
    val spikeOffsetY1 = high.max(low).toFloat()
    val spikeOffsetY2 = high.min(low).toFloat()
    val formattedFullDate: String by lazy { DateTimeFormats.fullDateWithDay.format(dateTime.toJavaLocalDateTime()) }

    override val price: BigDecimal = item.close

    override fun toString(): String {
        return "PriceItem(index=$index, date='$formattedFullDate', centerY:$centerY asset=$asset)"
    }

    override fun withCurrentMarketPrice(marketPrice: MarketPrice): PriceItemUI = PriceItemUI(
        index, asset, item.withCurrentMarketPrice(marketPrice)
    )
}

private fun BigDecimal.toScaleString(scale: Int = 6) = setScale(scale, RoundingMode.HALF_UP).toString()

fun PriceItemUI.priceDetails(): AnnotatedString {
    val spanStyle = SpanStyle(color = color)
    val scale = when {
        open > 100.bd -> 2
        open > 10.bd -> 3
        open > 1.bd -> 4
        open > 0.1.bd -> 5
        else -> 6
    }
    return AnnotatedString.Builder().apply {
        append("O")
        append(AnnotatedString(open.toScaleString(scale), spanStyle))
        append(" H")
        append(AnnotatedString(high.toScaleString(scale), spanStyle))
        append(" L")
        append(AnnotatedString(low.toScaleString(scale), spanStyle))
        append(" C")
        append(AnnotatedString(close.toScaleString(scale), spanStyle))
        if (!open.isZero()) {
            append(" %")
            val diffRatio = (close.setScale(3, RoundingMode.HALF_UP) / open)
                .let { if (it > BigDecimal.ONE) it - BigDecimal.ONE else -(BigDecimal.ONE - it) }
                .let { it * 100.bd }
                .toScaleString(2)
            append(AnnotatedString(diffRatio, spanStyle))
        }
    }.toAnnotatedString()
}
