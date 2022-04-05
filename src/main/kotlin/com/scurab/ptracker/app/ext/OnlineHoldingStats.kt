package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.MarketPercentage
import com.scurab.ptracker.app.model.OnlineHoldingStats

fun Collection<OnlineHoldingStats>.totalMarketValue(fiatCoin: FiatCoin? = null) = sumOf { it.marketValue(fiatCoin) }
fun Collection<OnlineHoldingStats>.totalCost(fiatCoin: FiatCoin? = null) = sumOf { it.cost(fiatCoin) }
fun Collection<OnlineHoldingStats>.totalGains(fiatCoin: FiatCoin? = null) = sumOf { it.gain(fiatCoin) }
fun Collection<OnlineHoldingStats>.marketPercentage(): List<MarketPercentage> {
    val totalMarketValue = totalMarketValue()
    return map { MarketPercentage(it.asset, it.marketValue.safeDiv(totalMarketValue).toFloat()) }
}

fun Collection<OnlineHoldingStats>.totalRoi(fiatCoin: FiatCoin? = null) = totalMarketValue(fiatCoin).safeDiv(totalCost(fiatCoin)).roi()
fun Collection<OnlineHoldingStats>.fiatCoins() = mapNotNull { it.asset.fiatCoinOrNull() }.distinct().sortedBy { it.item }
