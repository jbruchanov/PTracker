package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.OnlineHoldingStats

fun Collection<OnlineHoldingStats>.totalMarketValue(fiatCoin: FiatCoin? = null) = sumOf { it.marketValue(fiatCoin) }
fun Collection<OnlineHoldingStats>.totalCost(fiatCoin: FiatCoin? = null) = sumOf { it.cost(fiatCoin) }
fun Collection<OnlineHoldingStats>.totalGains(fiatCoin: FiatCoin? = null) = sumOf { it.gain(fiatCoin) }
fun Collection<OnlineHoldingStats>.marketPercentage() {
    val totalMarketValue = totalMarketValue()
    associateBy(
        keySelector = { it.asset },
        valueTransform = { it.marketValue.safeDiv(totalMarketValue) }
    )
}

fun Collection<OnlineHoldingStats>.totalRoi(fiatCoin: FiatCoin? = null) = totalMarketValue(fiatCoin).safeDiv(totalCost(fiatCoin)).roi()
fun Collection<OnlineHoldingStats>.fiatCoins() = map { it.asset.fiat }.distinct().sorted().map { FiatCoin(it) }