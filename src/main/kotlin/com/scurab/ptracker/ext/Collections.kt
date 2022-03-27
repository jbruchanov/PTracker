package com.scurab.ptracker.ext

import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.HasDateTime
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.ui.AppTheme.DashboardSizes
import com.scurab.ptracker.ui.DateTimeFormats
import com.scurab.ptracker.ui.priceboard.PriceBoardState
import kotlinx.datetime.toJavaLocalDateTime
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun List<PriceItem>.filterVisibleIndexes(state: PriceBoardState, step: Int = 1, startOffset: Int = 0, endOffset: Int = 0): IntProgression {
    val vp = state.viewport()
    val colWidth = DashboardSizes.PriceItemWidth
    val firstIndex = floor((max(0f, vp.left) / colWidth)).toInt()
    val widthToFill = vp.nWidth + min(vp.left, 0f)
    val count = ceil(min(widthToFill, size * colWidth) / colWidth).toInt()
    val lastIndex = (firstIndex + count).coerceAtMost(size)
    return (firstIndex + startOffset) until (lastIndex + endOffset) step step
}

fun List<PriceItem>.filterVisible(state: PriceBoardState, endOffset: Int = 0, step: Int = 1): List<PriceItem> {
    val range = filterVisibleIndexes(state, step, endOffset = endOffset)
    return filterIndexed { index, _ -> index in range }
}

fun List<PriceItem>.getHorizontalAxisText(index: Int, step: Int): String {
    val item = getOrNull(index) ?: return ""
    val prev = getOrNull(index - step)
    //TODO: handle also smaller candles than day
    val formatter = when {
        prev == null -> DateTimeFormats.monthYear
        item.dateTime.year != prev.dateTime.year -> DateTimeFormats.year
        item.dateTime.monthNumber != prev.dateTime.monthNumber -> DateTimeFormats.monthMid
        else -> DateTimeFormats.dayNumber
    }
    return formatter.format(item.dateTime.toJavaLocalDateTime())
}

fun List<HasDateTime>.firstIndexOf(other: HasDateTime, grouping: GroupStrategy): Int {
    val v = grouping.groupingKey(other.dateTime)
    return indexOfFirst { grouping.groupingKey(it.dateTime) == v }
}

fun <T> List<T>.takeAround(index: Int, n: Int): List<T> {
    require(index in indices) { "Invalid index:$index, must be in indices:${indices}" }
    val l = floor(n / 2f).toInt()
    val r = ceil(n / 2f).toInt()
    val le = index - l
    val re = index + r
    return when {
        n == 0 -> emptyList()
        n == 1 -> listOf(get(index))
        n >= size -> this.toList()
        le >= 0 && re < size -> this.subList(le, re)
        le < 0 -> subList(0, (re - le).coerceAtLeast(min(n, size)))
        re > size -> subList((le - (re - size)).coerceAtLeast(0), size)
        else -> TODO("size:$size, index:$index, n:$n")
    }
}
