package com.scurab.ptracker.ext

import com.scurab.ptracker.model.OnlineHoldingStats

fun Collection<OnlineHoldingStats>.totalMarketValue() = sumOf { it.marketValue }
fun Collection<OnlineHoldingStats>.totalCost() = sumOf { it.cost }
fun Collection<OnlineHoldingStats>.totalGains() = sumOf { it.gain }
fun Collection<OnlineHoldingStats>.marketPercentage() {
    val totalMarketValue = totalMarketValue()
    associateBy(
        keySelector = { it.marketPrice.asset },
        valueTransform = { it.marketValue.safeDiv(totalMarketValue) }
    )
}