package com.scurab.ptracker.model

import com.scurab.ptracker.ext.roi
import com.scurab.ptracker.ext.safeDiv
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

data class OnlineHoldingStats(
    val timeDate: LocalDateTime,
    private val holdings: Holdings,
    private val marketPriceItem: MarketPrice
) {
    val asset get() = marketPriceItem.asset
    val actualCryptoBalance get() = holdings.actualCryptoBalance
    val totalCryptoBalance get() = holdings.totalCryptoBalance
    val cost: BigDecimal get() = holdings.cost
    val pricePerUnit: BigDecimal get() = holdings.pricePerUnit
    val actualPricePerUnit: BigDecimal get() = holdings.actualPricePerUnit
    val freeIncome: BigDecimal get() = holdings.freeIncome
    val nonProfitableOutcome: BigDecimal get() = holdings.nonProfitableOutcome
    val balance get() = holdings.actualCryptoBalance
    val costUnit = cost.safeDiv(balance)
    val marketValue = actualCryptoBalance * marketPriceItem.price
    val marketValueUnitPrice = marketPriceItem.price
    val gain = marketValue - cost
    val roi = (marketValue.safeDiv(cost)).roi()

    fun marketValue(fiatCoin: FiatCoin?) = marketValue.takeIf { fiatCoin == null || asset.has(fiatCoin.item) } ?: BigDecimal.ZERO
    fun gain(fiatCoin: FiatCoin?) = gain.takeIf { fiatCoin == null || asset.has(fiatCoin.item) } ?: BigDecimal.ZERO
    fun cost(fiatCoin: FiatCoin?) = cost.takeIf { fiatCoin == null || asset.has(fiatCoin.item) } ?: BigDecimal.ZERO

    override fun toString(): String {
        return "OnlineHoldingStats(asset=${marketPriceItem.asset}, balance=$balance, cost=$cost, costUnit=$costUnit, marketValue=$marketValue, marketValuePriceUnit=$marketValueUnitPrice, gain=$gain, roi=$roi)"
    }
}