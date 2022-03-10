package com.scurab.ptracker.ext

import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.ui.PriceBoardState
import com.scurab.ptracker.ui.PriceDashboardSizes
import java.lang.Float
import kotlin.math.ceil

fun List<PriceItem>.getHovered(state: PriceBoardState): PriceItem? {
    return getOrNull(state.selectedPriceItemIndex())
}

fun List<PriceItem>.filterVisible(state: PriceBoardState): List<PriceItem> {
    val vp = state.viewPort()
    val colWidth = PriceDashboardSizes.PriceItemWidth
    val firstIndex = (Float.max(0f, vp.left) / colWidth).toInt()
    val widthToFill = vp.widthAbs + Float.min(vp.left, 0f)
    val count = ceil(Float.min(widthToFill, size * colWidth) / colWidth).toInt()
    val lastIndex = (firstIndex + count).coerceAtMost(size)
    val range = firstIndex until lastIndex
    return filterIndexed { index, _ -> index in range }
}