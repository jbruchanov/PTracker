package com.scurab.ptracker.app.ext

import androidx.compose.ui.graphics.Color
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.MarketPercentage
import com.scurab.ptracker.app.model.OnlineHoldingStats

fun Collection<OnlineHoldingStats>.totalMarketValue(fiatCoin: FiatCoin? = null) = sumOf { it.marketValue(fiatCoin) }
fun Collection<OnlineHoldingStats>.totalCost(fiatCoin: FiatCoin? = null) = sumOf { it.cost(fiatCoin) }
fun Collection<OnlineHoldingStats>.totalGains(fiatCoin: FiatCoin? = null) = sumOf { it.gain(fiatCoin) }
fun Collection<OnlineHoldingStats>.marketPercentage(): List<MarketPercentage> {
    val totalMarketValue = totalMarketValue()
    return map { MarketPercentage(it.asset, it.marketValue.safeDiv(totalMarketValue).toFloat()) }.sortedByDescending { it.percentage }
}

fun Collection<OnlineHoldingStats>.coloredMarketPercentage(groupingThreshold: Float = 0f): List<MarketPercentage> {
    val items = marketPercentage().sortedByDescending { it.percentage }
    val colors = items.map { it.asset }.colors()
    val result = mutableListOf<MarketPercentage>()
    items.forEachGrouping(groupingThreshold) { index, item, groupRest ->
        val color = if (groupRest) Color.White else colors[index]
        result.add(MarketPercentage(item.asset, item.percentage, color))
    }
    return result
}

fun Collection<OnlineHoldingStats>.totalRoi(fiatCoin: FiatCoin? = null) = totalMarketValue(fiatCoin).safeDiv(totalCost(fiatCoin)).roi()
fun Collection<OnlineHoldingStats>.fiatCoins() = mapNotNull { it.asset.fiatCoinOrNull() }.distinct().sortedBy { it.item }
