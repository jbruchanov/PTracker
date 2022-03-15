package com.scurab.ptracker.ext

import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.ui.DateFormats
import com.scurab.ptracker.ui.PriceBoardState
import com.scurab.ptracker.ui.PriceDashboardSizes
import kotlinx.datetime.toJavaLocalDateTime
import java.lang.Float.max
import java.lang.Float.min
import kotlin.Int
import kotlin.math.ceil

fun List<PriceItem>.filterVisibleIndexes(state: PriceBoardState, step: Int = 1, startOffset: Int = 0, endOffset: Int = 0): IntProgression {
    val vp = state.viewport()
    val colWidth = PriceDashboardSizes.PriceItemWidth
    val firstIndex = (max(0f, vp.left) / colWidth).toInt()
    val widthToFill = vp.nWidth + min(vp.left, 0f)
    val count = ceil(min(widthToFill, size * colWidth) / colWidth).toInt()
    val lastIndex = (firstIndex + count).coerceAtMost(size)
    return (firstIndex + startOffset) until (lastIndex + endOffset) step step
}

fun List<PriceItem>.filterVisible(state: PriceBoardState, step: Int = 1): List<PriceItem> {
    val range = filterVisibleIndexes(state, step)
    return filterIndexed { index, _ -> index in range }
}

fun List<PriceItem>.getHorizontalAxisText(index: Int, step: Int): String {
    val item = getOrNull(index) ?: return ""
    val prev = getOrNull(index - step)
    //TODO: handle also smaller candles than day
    val formatter = when {
        prev == null -> DateFormats.monthYear
        item.date.year != prev.date.year -> DateFormats.year
        item.date.monthNumber != prev.date.monthNumber -> DateFormats.monthMid
        else -> DateFormats.dayNumber
    }
    return formatter.format(item.date.toJavaLocalDateTime())
}
