package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import java.math.BigDecimal

data class MarketData(val cost: BigDecimal, val marketPrice: BigDecimal) {
    companion object {
        val Empty = MarketData(0.bd, 0.bd)
    }
}
