package com.scurab.ptracker.app.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
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
    override val dateTime: LocalDateTime, override val open: BigDecimal, override val close: BigDecimal, override val high: BigDecimal, override val low: BigDecimal
) : IPriceItem

fun randomPriceData(random: Random, count: Int, startDate: LocalDateTime, step: Duration): List<PriceItemUI> {
    var date: Instant = startDate.toInstant(TimeZone.UTC)
    return buildList {
        val coef = 5
        val asset = Asset("BTC", "GBP")
        repeat(count) {
            val open = lastOrNull()?.close ?: BigDecimal(0)
            val close = open + random.nextInt(-90 * coef, 100 * coef).toBigDecimal()
            val high = open.max(close) + random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            val low = open.min(close) - random.nextInt(20 * coef, 50 * coef).toBigDecimal()
            add(PriceItemUI(it, asset, PriceItem(dateTime = date.toLocalDateTime(TimeZone.UTC), open = open, close = close, high = high, low = low)))
            date = date.plus(step)
        }
    }
}
