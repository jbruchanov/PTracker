package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.LineChartData
import com.scurab.ptracker.app.model.Point
import com.scurab.ptracker.app.repository.AppStateRepository
import java.math.BigDecimal
import kotlin.math.abs

class StatsChartCalcUseCase(
    private val appStateRepository: AppStateRepository,
    private val statsCalculatorUseCase: StatsCalculatorUseCase,
) {

    fun getLineChartData(
        appData: AppData = appStateRepository.appData.value,
        primaryCurrency: String
    ): LineChartData {
        val stats = statsCalculatorUseCase.calculateMarketDailyGains(appData, primaryCurrency)
        if (stats.isEmpty()) return LineChartData.Empty
        val minYItem = stats.minBy { it.minOfCostOrPrice }
        val minY = minYItem.minOfCostOrPrice.toFloat()
        val maxYItem = stats.maxBy { it.maxOfCostOrPrice }
        val maxY = maxYItem.maxOfCostOrPrice.toFloat()
        val minYSinceMaxItem = stats.asSequence()
            .filter { it.localDateTime > maxYItem.localDateTime }
            .minByOrNull { it.minOfCostOrPrice }

        val requiredY = abs(maxY - minY)

        val marketPrice = mutableListOf<Point>()
        val cost = mutableListOf<Point>()

        fun point(index: Int, value: BigDecimal) = Point(
            x = (index.toFloat() / (stats.size - 1)),
            y = 1f - ((value.toFloat() - minY) / requiredY)
        )

        stats.forEachIndexed { index, dayStatsSum ->
            marketPrice.add(point(index, dayStatsSum.marketPrice))
            cost.add(point(index, dayStatsSum.cost))
        }

        val latestMarketPriceY = marketPrice.last().y
        val latestMarketPrice = listOf(
            Point(0f, latestMarketPriceY),
            Point(1f, latestMarketPriceY)
        )

        return LineChartData(
            stats, marketPrice, cost, latestMarketPrice,
            maxMarketPrice = point(stats.indexOf(maxYItem), maxYItem.maxOfCostOrPrice),
            minMarketPriceXSinceMax = minYSinceMaxItem?.let { it -> point(stats.indexOf(it), it.minOfCostOrPrice) }
        )
    }
}

