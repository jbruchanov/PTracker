package com.scurab.ptracker.app.model

import java.math.BigDecimal

data class CoinValue(val coin: String, val quantity: BigDecimal) {
    val isFiat by lazy { FiatCurrencies.contains(coin) }
    val isCrypto get() = !isFiat
}
