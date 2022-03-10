package com.scurab.ptracker.ext

import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.ui.PriceBoardState

fun List<PriceItem>.get(state: PriceBoardState): PriceItem? {
    return getOrNull(state.selectedPriceItemIndex())
}