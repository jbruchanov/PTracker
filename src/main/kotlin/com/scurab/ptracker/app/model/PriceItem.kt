package com.scurab.ptracker.app.model

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.AppTheme.DashboardColors
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.time.Duration

interface IPriceItem : HasDateTime {
    override val dateTime: LocalDateTime
    val open: BigDecimal
    val close: BigDecimal
    val high: BigDecimal
    val low: BigDecimal

    fun withCurrentMarketPrice(marketPrice: MarketPrice): IPriceItem {
        return object : IPriceItem {
            override val dateTime: LocalDateTime = this@IPriceItem.dateTime
            override val open: BigDecimal = this@IPriceItem.open
            override val close: BigDecimal = marketPrice.price
            override val high: BigDecimal = this@IPriceItem.high.max(marketPrice.price)
            override val low: BigDecimal = this@IPriceItem.low.min(marketPrice.price)
        }
    }

    companion object {
        fun MarketPrice.asPriceItem(dateTime: LocalDateTime) = object : IPriceItem {
            override val dateTime: LocalDateTime = dateTime
            override val open: BigDecimal = price
            override val close: BigDecimal = price
            override val high: BigDecimal = price
            override val low: BigDecimal = price
        }
    }
}

data class PriceItem(
    val index: Int, override val asset: Asset, val item: IPriceItem
) : IPriceItem by item, WithCache by MapCache(), MarketPrice {
    private val rectHeight = (open - close).abs().toFloat()
    val rectOffsetY = open.min(close).toFloat()
    val color = DashboardColors.Candle.default2If(open < close)
    val rectSize = Size(AppTheme.DashboardSizes.PriceItemWidth, rectHeight)
    val centerY = (open + close).toFloat() / 2f
    val spikeOffsetY1 = high.max(low).toFloat()
    val spikeOffsetY2 = high.min(low).toFloat()
    val formattedFullDate: String by lazy { DateTimeFormats.fullDate.format(dateTime.toJavaLocalDateTime()) }

    override val price: BigDecimal = item.close

    override fun toString(): String {
        return "PriceItem(index=$index, date='$formattedFullDate', centerY:${centerY} asset=$asset)"
    }

    override fun withCurrentMarketPrice(marketPrice: MarketPrice): PriceItem = PriceItem(
        index, asset, item.withCurrentMarketPrice(marketPrice)
    )
}

data class TestPriceItem(
    override val dateTime: LocalDateTime, override val open: BigDecimal, override val close: BigDecimal, override val high: BigDecimal, override val low: BigDecimal
) : IPriceItem, WithCache by MapCache() {
    override fun withCurrentMarketPrice(marketPrice: MarketPrice): IPriceItem {
        TODO("Not yet implemented")
    }
}

fun randomPriceData(random: Random, count: Int, startDate: LocalDateTime, step: Duration): List<PriceItem> {
    var date: Instant = startDate.toInstant(TimeZone.UTC)
    return buildList {
        val coef = 5
        val asset = Asset("BTC", "GBP")
        repeat(count) {
            val open = lastOrNull()?.close ?: BigDecimal(0)
            val close = open + random.nextInt(-90 * coef, 100 * coef).toBigDecimal()
            val high = open.max(close) + random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            val low = open.min(close) - random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            add(PriceItem(it, asset, TestPriceItem(dateTime = date.toLocalDateTime(TimeZone.UTC), open = open, close = close, high = high, low = low)))
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
        if (!open.isZero()) {
            append("  ")
            append(AnnotatedString((close.setScale(3, RoundingMode.HALF_UP) / open).toScaleString(2) + "%", spanStyle))
        }
    }.toAnnotatedString()
}