package com.scurab.ptracker.ui.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.roundToPx
import com.scurab.ptracker.ui.AppTheme
import kotlin.math.max

interface TableScope
object TableScopeInstance : TableScope

interface ICellInfo {
    val row: Int
    val column: Int
}

data class CellInfo(override val row: Int, override val column: Int) : ICellInfo

@Composable
fun Table(
    data: TableData,
    columnWidthStrategy: ColumnWidthStrategy,
    modifier: Modifier = Modifier,
    cell: @Composable TableScope.(ICellInfo) -> Unit
) {
    Layout(
        content = {
            data.forEach { column, row ->
                TableScopeInstance.cell(CellInfo(row, column))
            }
        },
        modifier = modifier,
        measurePolicy = columnWidthStrategy
    )
}

interface TableData {
    val columns: Int
    val rows: Int
    fun getItem(column: Int, row: Int): Any?
    fun getItem(cellInfo: ICellInfo) = getItem(cellInfo.column, cellInfo.row)
}

inline fun TableData.forEach(block: (column: Int, row: Int) -> Unit) {
    (0 until rows).forEach { row ->
        (0 until columns).forEach { column ->
            block(column, row)
        }
    }
}


interface ColumnWidthStrategy : MeasurePolicy

sealed class ColumnWidths : ColumnWidthStrategy {
    class ExactWidth(private val widths: List<Int>) : ColumnWidths() {
        private val columns = widths.size
        private val width = widths.sum()
        override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
            val rows = measurables.windowed(columns, columns)
            val availableHeightPerRow = constraints.maxHeight / rows.size

            val placeables = rows.map { row ->
                row.mapIndexed { colIndex, measurable ->
                    measurable.measure(Constraints(minWidth = widths[colIndex], maxHeight = availableHeightPerRow))
                }
            }

            val heights = placeables.map { row -> row.maxOf { placeable -> placeable.height } }
            val height = heights.sum()

            return layout(max(width, constraints.maxWidth), max(height, constraints.maxHeight)) {
                var y = 0
                var x = 0
                placeables.forEachIndexed { rowIndex, columns ->
                    x = 0
                    columns.forEachIndexed { colIndex, placeables ->
                        placeables.place(x, y)
                        x += widths[colIndex]
                    }
                    y += heights[rowIndex]
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewTable() {
    val data = object : TableData {
        val items = listOf(
            listOf("A", "B\nB", "C"),
            listOf("1", "2", "3"),
            listOf("A", "B\nB", "C"),
            listOf("4", "5", "6"),
        )

        override val columns: Int = 3
        override val rows: Int = items.size
        override fun getItem(column: Int, row: Int): String = items[row][column]
    }
    AppTheme {
        val backgrounds = listOf(
            listOf(Color.Gray, Color.DarkGray, Color.Gray),
            listOf(Color.Gray.copy(alpha = 0.5f), Color.DarkGray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f))
        )
        Box {
            Table(
                data,
                columnWidthStrategy = ColumnWidths.ExactWidth(listOf(30.dp, 70.dp, 50.dp).map { it.roundToPx(LocalDensity.current.density) }),
                modifier = Modifier
            ) { cellInfo ->
                Text(
                    data.getItem(cellInfo).toString(), modifier = Modifier
                        .background(backgrounds[cellInfo.row % 2][cellInfo.column])
                        .fillMaxSize()
                        .wrapContentSize(align = Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}