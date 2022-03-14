package com.scurab.ptracker.ext

import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.ui.PriceBoardState
import com.scurab.ptracker.ui.PriceDashboardSizes
import com.scurab.ptracker.ui.viewport
import java.lang.Float
import kotlin.Int
import kotlin.math.ceil

fun List<PriceItem>.getHovered(state: PriceBoardState): PriceItem? {
    return getOrNull(state.selectedPriceItemIndex())
}

fun List<PriceItem>.filterVisibleIndexes(state: PriceBoardState, step: Int = 1, startOffset : Int = 0, endOffset: Int = 0): IntProgression {
    val vp = state.viewport()
    val colWidth = PriceDashboardSizes.PriceItemWidth
    val firstIndex = (Float.max(0f, vp.left) / colWidth).toInt()
    val widthToFill = vp.nWidth + Float.min(vp.left, 0f)
    val count = ceil(Float.min(widthToFill, size * colWidth) / colWidth).toInt()
    val lastIndex = (firstIndex + count).coerceAtMost(size)
    //return (firstIndex + startOffset) until (lastIndex + endOffset) step step
    return indices step step
}

fun List<PriceItem>.filterVisible(state: PriceBoardState, step: Int = 1): List<PriceItem> {
    val range = filterVisibleIndexes(state, step)
    return filterIndexed { index, _ -> index in range }
}