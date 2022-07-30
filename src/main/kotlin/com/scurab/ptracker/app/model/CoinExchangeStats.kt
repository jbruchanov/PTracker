package com.scurab.ptracker.app.model

import java.math.BigDecimal

data class CoinExchangeStats(
    val coin: AnyCoin,
    val exchange: ExchangeWallet,
    val quantity: BigDecimal,
    val perc: BigDecimal,
    val price: BigDecimal?
)