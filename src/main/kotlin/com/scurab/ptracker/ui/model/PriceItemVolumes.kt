package com.scurab.ptracker.ui.model

import com.scurab.ptracker.app.model.PriceItem
import java.math.BigDecimal

data class PriceItemVolumes(
    val priceItem: PriceItem,
    val coin1Volume: BigDecimal,
    val coin2Volume: BigDecimal
)