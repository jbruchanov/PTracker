package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.safeDiv
import java.math.BigDecimal

data class Holdings(
    val asset: Asset,
    val actualCryptoBalance: BigDecimal,
    val totalCryptoBalance: BigDecimal,
    val cost: BigDecimal
) {
    val pricePerUnit = cost.safeDiv(totalCryptoBalance)
    val actualPricePerUnit = cost.safeDiv(actualCryptoBalance)

    //staking, gifts or anything what wasn't paid for
    val freeIncome = (actualCryptoBalance - totalCryptoBalance).max(BigDecimal.ZERO)

    //gifts, losses
    val nonProfitableOutcome = (actualCryptoBalance - totalCryptoBalance).min(BigDecimal.ZERO).abs()

    fun realtimeStats(marketPrice: MarketPrice): OnlineHoldingStats {
        require(asset == marketPrice.asset) { "Invalid marketPrice:${marketPrice.asset}, expected:${asset}" }
        return OnlineHoldingStats(now(), this, marketPrice)
    }

    override fun toString(): String {
        return "Holdings(asset=$asset, actualCryptoBalance=$actualCryptoBalance, totalCryptoBalance=$totalCryptoBalance, cost=$cost, pricePerUnit=$pricePerUnit, actualPricePerUnit=$actualPricePerUnit, freeIncome=$freeIncome, nonProfitableOutcome=$nonProfitableOutcome)"
    }
}


