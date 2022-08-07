package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.PriceHistoryChartData
import com.scurab.ptracker.app.model.Point
import com.scurab.ptracker.app.model.PriceItemUI
import com.scurab.ptracker.app.model.Transaction
import java.math.BigDecimal
import kotlin.math.abs

class StatsChartCalcUseCase(
    private val statsCalculatorUseCase: StatsCalculatorUseCase,
) {
    fun getLineChartData(
        appData: AppData,
        primaryCurrency: String,
    ): PriceHistoryChartData = getLineChartData(appData.ledger.items, appData.historyPrices, primaryCurrency)

    fun getLineChartData(
        transactions: List<Transaction>,
        prices: Map<Asset, List<PriceItemUI>>,
        primaryCurrency: String,
        dateGrouping: DateGrouping = DateGrouping.Day
    ): PriceHistoryChartData {
        val assets = transactions.setOf { it.asset }
        val doSumCrypto = assets.size == 1
        val stats = statsCalculatorUseCase.calculateMarketDailyGains(transactions, prices, primaryCurrency, dateGrouping, doSumCrypto)
        if (stats.isEmpty()) return PriceHistoryChartData.Empty
        val minYItem = stats.minBy { it.minOfCostOrPrice }
        val minY = minYItem.minOfCostOrPrice.toFloat()
        val maxYItem = stats.maxBy { it.maxOfCostOrPrice }
        val maxY = maxYItem.maxOfCostOrPrice.toFloat()
        val minYSinceMaxItem = stats.asSequence()
            .filter { it.localDateTime > maxYItem.localDateTime }
            .minByOrNull { it.minOfCostOrPrice }

        val requiredY = abs(maxY - minY)
        val avgY = if (doSumCrypto) {
            val asset = assets.first()
            val diff = prices.getValue(asset).maxOf { it.price } - prices.getValue(asset).minOf { it.price }
            diff.abs().toFloat()
        } else 0f

        val marketPrice = mutableListOf<Point>()
        val cost = mutableListOf<Point>()
        val avg = mutableListOf<Point>()

        fun point(index: Int, value: BigDecimal, yScale: Float = requiredY) = Point(
            x = (index.toFloat() / (stats.size - 1)),
            y = 1f - ((value.toFloat() - minY) / yScale)
        )

        stats.forEachIndexed { index, dayStatsSum ->
            marketPrice.add(point(index, dayStatsSum.marketValue))
            cost.add(point(index, dayStatsSum.cost))
            if (doSumCrypto) {
                avg.add(point(index, dayStatsSum.avgCryptoPrice, avgY))
            }
        }

        val latestMarketPriceY = marketPrice.last().y
        val latestMarketPrice = listOf(
            Point(0f, latestMarketPriceY),
            Point(1f, latestMarketPriceY)
        )

        return PriceHistoryChartData(
            stats, marketPrice, cost, avg, latestMarketPrice,
            maxMarketPrice = point(stats.indexOf(maxYItem), maxYItem.maxOfCostOrPrice),
            minMarketPriceXSinceMax = minYSinceMaxItem?.let { it -> point(stats.indexOf(it), it.minOfCostOrPrice) }
        )
    }
}

