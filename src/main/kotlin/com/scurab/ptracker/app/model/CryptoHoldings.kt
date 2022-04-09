package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.ext.align
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.safeDiv
import java.math.BigDecimal

data class CryptoHoldings(
    val asset: Asset,
    val actualCryptoBalance: BigDecimal,
    val totalCryptoBalance: BigDecimal,
    val cost: BigDecimal,
    val feesCrypto: BigDecimal
) {
    val pricePerUnit = cost.safeDiv(totalCryptoBalance)
    val actualPricePerUnit = cost.safeDiv(actualCryptoBalance)

    //staking, gifts or anything what wasn't paid for
    val freeIncome = (actualCryptoBalance.align - totalCryptoBalance).max(ZERO)

    //gifts, losses
    val nonProfitableOutcome = (actualCryptoBalance.align - totalCryptoBalance).min(ZERO).abs()

    fun realtimeStats(marketPrice: MarketPrice): OnlineHoldingStats {
        require(asset == marketPrice.asset) { "Invalid marketPrice:${marketPrice.asset}, expected:${asset}" }
        return OnlineHoldingStats(now(), this, marketPrice)
    }

    override fun toString(): String {
        return "Holdings(asset=$asset, actualCryptoBalance=$actualCryptoBalance, totalCryptoBalance=$totalCryptoBalance, cost=$cost, pricePerUnit=$pricePerUnit, actualPricePerUnit=$actualPricePerUnit, freeIncome=$freeIncome, nonProfitableOutcome=$nonProfitableOutcome)"
    }
}


