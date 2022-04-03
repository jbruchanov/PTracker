package com.scurab.ptracker.app.model

import java.math.BigDecimal

interface MarketPrice {
    val asset: Asset
    val price: BigDecimal
}