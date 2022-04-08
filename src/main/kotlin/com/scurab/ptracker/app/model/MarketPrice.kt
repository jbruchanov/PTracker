package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.inverse
import java.math.BigDecimal

interface MarketPrice {
    val asset: Asset
    val price: BigDecimal

    fun flipAsset() = CoinPrice(asset.flipCoins(), price.inverse())
}