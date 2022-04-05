package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.serialisation.BigDecimalAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CoinPrice(
    @SerialName("asset") override val asset: Asset,
    @SerialName("price") @Serializable(with = BigDecimalAsStringSerializer::class) override val price: BigDecimal
) : MarketPrice {

    companion object {
        fun MarketPrice.asCoinPrice() = if (this is CoinPrice) this else CoinPrice(asset, price)
    }
}