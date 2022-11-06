package com.scurab.ptracker.ui.model

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min

sealed class TableCellSize {
    abstract val minWidth: Dp

    abstract fun realisticSize(): Dp

    data class Exact(val size: Dp, override val minWidth: Dp = min(size, DefaultMinWidth)) : TableCellSize() {
        override fun realisticSize(): Dp = max(size, minWidth)
    }

    data class Percents(val weight: Float, override val minWidth: Dp = DefaultMinWidth) : TableCellSize() {
        override fun realisticSize(): Dp = minWidth
    }

    companion object {
        val DefaultMinWidth = 64.dp
        val Zero = Exact(0.dp, 0.dp)
    }
}

@Composable
fun Modifier.height(tableCellSize: TableCellSize.Exact): Modifier = height(tableCellSize.size * LocalDensity.current.fontScale)

context(RowScope)
@Composable
fun Modifier.width(tableCellSize: TableCellSize): Modifier = when (tableCellSize) {
    is TableCellSize.Exact -> defaultMinSize(minWidth = tableCellSize.minWidth * LocalDensity.current.fontScale).width(tableCellSize.size * LocalDensity.current.fontScale)
    is TableCellSize.Percents ->
        this
            .weight(tableCellSize.weight, fill = true)
            .widthIn(tableCellSize.minWidth)
}
