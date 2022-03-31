package com.scurab.ptracker.model

import java.math.BigDecimal

data class CoinPrice(override val asset: Asset, override val price: BigDecimal) : MarketPrice