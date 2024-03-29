package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.ext.roi
import com.scurab.ptracker.app.ext.safeDiv
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode

data class OnlineHoldingStats(
    val timeDate: LocalDateTime,
    private val holdings: CryptoHoldings,
    private val marketPriceItem: MarketPrice
) {
    val asset get() = marketPriceItem.asset
    val actualCryptoBalance get() = holdings.actualCryptoBalance
    val totalCryptoBalance get() = holdings.totalCryptoBalance
    val cost: BigDecimal get() = holdings.cost
    val pricePerUnit: BigDecimal get() = holdings.pricePerUnit
    val actualPricePerUnit: BigDecimal get() = holdings.actualPricePerUnit
    val freeIncome: BigDecimal get() = holdings.freeIncome
    val freeIncomeMarketPrice: BigDecimal get() = holdings.freeIncome * marketPriceItem.price
    val nonProfitableOutcome: BigDecimal get() = -holdings.nonProfitableOutcome
    val nonProfitableOutcomeMarketPrice: BigDecimal get() = nonProfitableOutcome * marketPriceItem.price
    val balance get() = holdings.actualCryptoBalance
    val costUnit = cost.safeDiv(balance)
    val costTotalUnit = cost.safeDiv(totalCryptoBalance)
    val marketValue = (actualCryptoBalance * marketPriceItem.price).setScale(4, RoundingMode.HALF_UP)
    val totalMarketValue = (totalCryptoBalance * marketPriceItem.price).setScale(4, RoundingMode.HALF_UP)
    val marketValueUnitPrice = marketPriceItem.price
    val gain = marketValue - cost
    val roi = (marketValue.safeDiv(cost)).roi()
    val feesCrypto = holdings.feesCrypto
    val feesCryptoMarketValue = feesCrypto * marketValueUnitPrice

    fun marketValue(fiatCoin: FiatCoin?) = marketValue.takeIf { fiatCoin == null || asset.has(fiatCoin.item) } ?: ZERO
    fun gain(fiatCoin: FiatCoin?) = gain.takeIf { fiatCoin == null || asset.has(fiatCoin.item) } ?: ZERO
    fun cost(fiatCoin: FiatCoin?) = cost.takeIf { fiatCoin == null || asset.has(fiatCoin.item) } ?: ZERO

    override fun toString(): String {
        return "OnlineHoldingStats(asset=${marketPriceItem.asset}, " +
            "balance=$balance, " +
            "cost=$cost, " +
            "costUnit=$costUnit, " +
            "marketValue=$marketValue, " +
            "marketValuePriceUnit=$marketValueUnitPrice, " +
            "gain=$gain, " +
            "roi=$roi)"
    }
}
