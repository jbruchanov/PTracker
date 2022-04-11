package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.LineChartData
import com.scurab.ptracker.app.model.Point
import com.scurab.ptracker.app.repository.AppStateRepository
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
        val minY = stats.minOf { it.cost.min(it.marketPrice) }.toFloat()
        val maxY = stats.maxOf { it.cost.max(it.marketPrice) }.toFloat()
        val requiredY = abs(maxY - minY)

        val marketPrice = mutableListOf<Point>()
        val cost = mutableListOf<Point>()

        stats.forEachIndexed { index, dayStatsSum ->
            val x = (index.toFloat() / (stats.size - 1))
            //reverse y
            val yMarketPrice = 1f - ((dayStatsSum.marketPrice.toFloat() - minY) / requiredY)
            val yCost = 1f - ((dayStatsSum.cost.toFloat() - minY) / requiredY)
            marketPrice.add(Point(x, yMarketPrice))
            cost.add(Point(x, yCost))
        }

        return LineChartData(marketPrice, cost)
    }
}

