package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.atDayOfMonth
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.ui.Texts
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

class PriceHistoryChartData(
    val stats: List<GroupStatsSum>,
    val marketPrice: List<Point>,
    val cost: List<Point>,
    val avg: List<Point>,
    val latestMarketPrice: List<Point>,
    val maxMarketPrice: Point?,
    val minMarketPriceXSinceMax: Point?
) {
    //y flipped for canvas
    val hasProfit = (marketPrice.lastOrNull()?.y ?: 0f) < (cost.lastOrNull()?.y ?: 0f)

    fun historyStats(texts: Texts): List<Pair<String, GroupStatsSum>> = buildList {
        val today = now().date
        val dates = listOf(
            texts.Today to today,
            texts.Yesterday to today.minus(1, DateTimeUnit.DAY),
            null to today.minus(1, DateTimeUnit.WEEK),
            null to today.minus(1, DateTimeUnit.MONTH).atDayOfMonth(1),
            null to today.minus(6, DateTimeUnit.MONTH).atDayOfMonth(1),
            null to today.minus(1, DateTimeUnit.YEAR).atDayOfMonth(1)
        )
        dates.forEach { (dayName, date) ->
            stats.getOrNull(stats.indexOfFirst { it.localDateTime.date == date })?.let { add((dayName ?: it.formattedDateTime) to it) }
        }
    }

    companion object {
        val Empty = PriceHistoryChartData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), Point.Empty, Point.Empty)
    }
}