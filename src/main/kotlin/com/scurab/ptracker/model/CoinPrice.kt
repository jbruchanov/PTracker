package com.scurab.ptracker.model

import com.scurab.ptracker.serialisation.BigDecimalAsStringSerializer
import kotlinx.serialization.SerialName
import java.math.BigDecimal
import kotlinx.serialization.Serializable

@Serializable
data class CoinPrice(
    @SerialName("asset") override val asset: Asset,
    @SerialName("price") @Serializable(with = BigDecimalAsStringSerializer::class) override val price: BigDecimal
) : MarketPrice