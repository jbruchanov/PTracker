package com.scurab.ptracker.ui.model

import com.scurab.ptracker.app.model.PriceItemUI
import java.math.BigDecimal

data class PriceItemVolumes(
    val priceItem: PriceItemUI,
    val coin1Volume: BigDecimal,
    val coin2Volume: BigDecimal
)
