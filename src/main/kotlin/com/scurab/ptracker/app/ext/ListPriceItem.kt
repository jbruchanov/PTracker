package com.scurab.ptracker.app.ext

import androidx.compose.ui.geometry.Rect
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.model.SimplePriceItem
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import com.scurab.ptracker.ui.priceboard.PriceBoardState
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun List<PriceItem>.filterVisibleIndexes(state: PriceBoardState, step: Int = 1, startOffset: Int = 0, endOffset: Int = 0): IntProgression =
    filterVisibleIndexes(state.viewport(), step, startOffset, endOffset)


fun List<PriceItem>.filterVisibleIndexes(viewPort: Rect, step: Int = 1, startOffset: Int = 0, endOffset: Int = 0): IntProgression {
    val colWidth = AppTheme.DashboardSizes.PriceItemWidth
    val firstIndex = floor((max(0f, viewPort.left) / colWidth)).toInt()
    val widthToFill = viewPort.nWidth + min(viewPort.left, 0f)
    val count = ceil(min(widthToFill, size * colWidth) / colWidth).toInt()
    val lastIndex = (firstIndex + count).coerceAtMost(size)
    return (firstIndex + startOffset) until (lastIndex + endOffset) step step
}

fun List<PriceItem>.filterVisible(state: PriceBoardState, endOffset: Int = 0, step: Int = 1): List<PriceItem> =
    filterVisible(state.viewport(), endOffset, step)

fun List<PriceItem>.filterVisible(viewPort: Rect, endOffset: Int = 0, step: Int = 1): List<PriceItem> {
    val range = filterVisibleIndexes(viewPort, step, endOffset = endOffset)
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

fun List<PriceItem>.average(dateTime: LocalDateTime? = null): PriceItem {
    require(isNotEmpty()) { "Collection is empty" }
    val assets = this.setOf { it.asset }
    require(assets.size == 1) { "Invalid data, asset must be unique per collection, has:$assets" }

    val first = first()
    return PriceItem(0, assets.first(),
        SimplePriceItem(
            dateTime = dateTime ?: first.dateTime,
            open = first.open,
            close = last().close,
            high = maxOf { it.high },
            low = minOf { it.low }
        ))
}