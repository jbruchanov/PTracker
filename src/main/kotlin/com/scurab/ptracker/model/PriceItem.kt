package com.scurab.ptracker.model

import androidx.compose.ui.geometry.Size
import com.scurab.ptracker.ui.PriceDashboardColor
import com.scurab.ptracker.ui.PriceDashboardSizes
import com.scurab.ptracker.ui.dateFormat
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import kotlin.random.Random
import kotlin.time.Duration

data class PriceItem(
    val index: Int,
    val time: LocalDateTime,
    val open: BigDecimal,
    val close: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
) {
    private val rectHeight = (open - close).abs().toFloat()
    val rectOffsetY = open.min(close).toFloat()
    val color = if (open >= close) PriceDashboardColor.CandleRed else PriceDashboardColor.CandleGreen
    val rectSize = Size(PriceDashboardSizes.PriceItemWidth, rectHeight)
    val spikeOffsetY1 = high.max(low).toFloat()
    val spikeOffsetY2 = high.min(low).toFloat()
    val renderDate: String by lazy { dateFormat.format(time.toJavaLocalDateTime()) }
}

fun randomPriceData(random: Random, count: Int, startDate: LocalDateTime, step: Duration): List<PriceItem> {
    var date: Instant = startDate.toInstant(TimeZone.UTC)
    return buildList {
        val coef = 5
        repeat(count) {
            val open = lastOrNull()?.close ?: BigDecimal(0)
            val close = open + random.nextInt(-90 * coef, 100 * coef).toBigDecimal()
            val high = open.max(close) + random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            val low = open.min(close) - random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            add(PriceItem(it, time = date.toLocalDateTime(TimeZone.UTC), open = open, close = close, high = high, low = low))
            date = date.plus(step)
        }
    }
}