package com.scurab.ptracker.model

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.AppTheme.DashboardColors
import com.scurab.ptracker.ui.DateFormats
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random
import kotlin.time.Duration

interface IPriceItem {
    val date: LocalDateTime
    val open: BigDecimal
    val close: BigDecimal
    val high: BigDecimal
    val low: BigDecimal
}

class PriceItem(
    val index: Int,
    val item: IPriceItem
) : IPriceItem by item {
    private val rectHeight = (open - close).abs().toFloat()
    val rectOffsetY = open.min(close).toFloat()
    val color = if (open >= close) DashboardColors.CandleRed else DashboardColors.CandleGreen
    val rectSize = Size(AppTheme.DashboardSizes.PriceItemWidth, rectHeight)
    val centerY = (open + close).toFloat() / 2f
    val spikeOffsetY1 = high.max(low).toFloat()
    val spikeOffsetY2 = high.min(low).toFloat()
    val fullDate: String by lazy { DateFormats.fullDate.format(date.toJavaLocalDateTime()) }
}

data class TestPriceItem(
    override val date: LocalDateTime,
    override val open: BigDecimal,
    override val close: BigDecimal,
    override val high: BigDecimal,
    override val low: BigDecimal
) : IPriceItem

fun randomPriceData(random: Random, count: Int, startDate: LocalDateTime, step: Duration): List<PriceItem> {
    var date: Instant = startDate.toInstant(TimeZone.UTC)
    return buildList {
        val coef = 5
        repeat(count) {
            val open = lastOrNull()?.close ?: BigDecimal(0)
            val close = open + random.nextInt(-90 * coef, 100 * coef).toBigDecimal()
            val high = open.max(close) + random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            val low = open.min(close) - random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            add(PriceItem(it, TestPriceItem(date = date.toLocalDateTime(TimeZone.UTC), open = open, close = close, high = high, low = low)))
            date = date.plus(step)
        }
    }
}

private fun BigDecimal.toScaleString(scale: Int = 6) = setScale(scale, RoundingMode.HALF_UP).toString()

fun PriceItem.priceDetails(): AnnotatedString {
    val spanStyle = SpanStyle(color = color)
    return AnnotatedString.Builder().apply {
        append("O")
        append(AnnotatedString(open.toScaleString(), spanStyle))
        append(" H")
        append(AnnotatedString(high.toScaleString(), spanStyle))
        append(" L")
        append(AnnotatedString(low.toScaleString(), spanStyle))
        append(" C")
        append(AnnotatedString(close.toScaleString(), spanStyle))
        if (open != BigDecimal.ZERO) {
            append("  ")
            append(AnnotatedString((close.setScale(3, RoundingMode.HALF_UP) / open).toScaleString(2) + "%", spanStyle))
        }
    }.toAnnotatedString()
}