package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import java.math.BigDecimal

/**
 * @param sumCrypto only if calculated for 1 crypto coin
 */
data class MarketData(
    val cost: BigDecimal,
    val marketValue: BigDecimal,
    val sumCrypto: BigDecimal
) {
    companion object {
        val Empty = MarketData(0.bd, 0.bd, 0.bd)
    }
}
