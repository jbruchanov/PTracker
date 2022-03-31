package com.scurab.ptracker.model

import java.math.BigDecimal

interface MarketPrice {
    val asset: Asset
    val price: BigDecimal
}