package com.scurab.ptracker.model

import com.scurab.ptracker.ext.bd
import com.scurab.ptracker.ext.isZero
import com.scurab.ptracker.ext.safeDiv
import java.math.BigDecimal

data class OnlineHoldingStats(
    val holdings: Holdings,
    val marketPrice: MarketPrice
) {
    val balance = holdings.actualCryptoBalance
    val cost = holdings.spentFiat
    val costUnit = cost.safeDiv(balance)
    val marketValue = holdings.actualCryptoBalance * marketPrice.price
    val marketValueUnitPrice = marketPrice.price
    val gain = marketValue - cost
    val roi = (marketValue.safeDiv(cost))
        .takeIf { !it.isZero() }
        ?.let { v -> if (v > 1.bd) v - 1.bd else -(1.bd - v) }
        ?: BigDecimal.ZERO

    override fun toString(): String {
        return "OnlineHoldingStats(asset=${marketPrice.asset}, balance=$balance, cost=$cost, costUnit=$costUnit, marketValue=$marketValue, marketValuePriceUnit=$marketValueUnitPrice, gain=$gain, roi=$roi)"
    }
}