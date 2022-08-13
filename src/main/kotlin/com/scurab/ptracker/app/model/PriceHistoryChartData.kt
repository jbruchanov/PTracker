package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.atDayOfMonth
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.ui.Texts
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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

    fun historyStats(texts: Texts): List<Pair<String, GroupStatsSum>> {
        if (stats.isEmpty()) return emptyList()

        val subResult = mutableListOf<GroupStatsSum>()
        val today = now().date
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        val dates = listOf(
            today,
            today.minus(1, DateTimeUnit.WEEK),
            today.minus(1, DateTimeUnit.MONTH).atDayOfMonth(1),
            today.minus(6, DateTimeUnit.MONTH).atDayOfMonth(1),
            today.minus(1, DateTimeUnit.YEAR).atDayOfMonth(1)
        )
        dates.forEach { date ->
            stats.getOrNull(stats.indexOfLast { it.localDateTime.date == date })?.let { subResult.add(it) }
        }
        stats.lastOrNull()
            ?.let { last -> stats.lastOrNull { it.avgCryptoPrice != last.avgCryptoPrice } }
            .let { lastWithDiffAvgPrice -> lastWithDiffAvgPrice ?: stats.lastOrNull { it.localDateTime.date == yesterday } }
            ?.also {
                subResult.add(it)
            }

        return subResult
            .distinctBy { it.localDateTime.date }
            .sortedByDescending { it.localDateTime }
            .map { statsSum ->
                val label = when (statsSum.localDateTime.date) {
                    today -> texts.Today
                    yesterday -> texts.Yesterday
                    else -> statsSum.formattedDateTime
                }
                Pair(label, statsSum)
            }
    }

    companion object {
        val Empty = PriceHistoryChartData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), Point.Empty, Point.Empty)
    }
}