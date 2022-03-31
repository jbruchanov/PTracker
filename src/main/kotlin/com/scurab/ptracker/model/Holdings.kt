package com.scurab.ptracker.model

import com.scurab.ptracker.ext.safeDiv
import java.math.BigDecimal

data class Holdings(
    val asset: Asset,
    val actualCryptoBalance: BigDecimal,
    val totalCryptoBalance: BigDecimal,
    val spentFiat: BigDecimal
) {
    val pricePerUnit = spentFiat.safeDiv(totalCryptoBalance)
    val actualPricePerUnit = spentFiat.safeDiv(actualCryptoBalance)

    //staking, gifts or anything what wasn't paid for
    val freeIncome = (actualCryptoBalance - totalCryptoBalance).max(BigDecimal.ZERO)

    //gifts, losses
    val nonProfitableOutcome = (actualCryptoBalance - totalCryptoBalance).min(BigDecimal.ZERO).abs()

    fun realtimeStats(marketPrice: MarketPrice): OnlineHoldingStats {
        require(asset == marketPrice.asset) { "Invalid marketPrice:${marketPrice.asset}, expected:${asset}" }
        return OnlineHoldingStats(this, marketPrice)
    }

    override fun toString(): String {
        return "Holdings(asset=$asset, actualCryptoBalance=$actualCryptoBalance, totalCryptoBalance=$totalCryptoBalance, spentFiat=$spentFiat, pricePerUnit=$pricePerUnit, actualPricePerUnit=$actualPricePerUnit, freeIncome=$freeIncome, nonProfitableOutcome=$nonProfitableOutcome)"
    }
}


