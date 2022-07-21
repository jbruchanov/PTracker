package com.scurab.ptracker.ui.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.scurab.ptracker.app.ext.sumOfDps
import com.scurab.ptracker.app.ext.toDp
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.model.ITableConfig
import com.scurab.ptracker.ui.model.ITableData
import com.scurab.ptracker.ui.model.ITableMetaData
import com.scurab.ptracker.ui.model.ITableState
import com.scurab.ptracker.ui.model.TableCellSize
import com.scurab.ptracker.ui.model.TableConfig
import com.scurab.ptracker.ui.model.TableState
import com.scurab.ptracker.ui.model.height
import com.scurab.ptracker.ui.model.width


@Composable
private fun defaultLazyTableCell(text: String) {
    FastText(
        text,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun defaultLazyTableHeaderCell(text: String) {
    Text(
        text = text,
        maxLines = 1,
        style = AppTheme.TextStyles.Small,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = AppSizes.current.Padding)
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun LazyTable(
    data: ITableData,
    tableState: ITableState = TableState.default(),
    tableConfig: ITableConfig = TableConfig.default(),
    modifier: Modifier = Modifier,
    headerCell: @Composable (Int) -> Unit = { defaultLazyTableHeaderCell(data.metaData.getHeaderTitle(it)) },
    cell: @Composable (Int, Int) -> Unit = { rowIndex, columnIndex -> defaultLazyTableCell(data[rowIndex, columnIndex]) },
) {

    val columnWidths = remember(data.rows) {
        Array(data.columns) { data.metaData.getColumnWidth(it, data) }
    }
    val requiredMinWidth = remember(columnWidths) {
        columnWidths.asIterable().sumOfDps { it.realisticSize() }
    }

    BoxWithConstraints {
        val density = LocalDensity.current.density
        /* extra 1dp to minimize the scrollbar visibility during window resizing */
        val tableWidth = max(requiredMinWidth, constraints.maxWidth.toDp(density)) - 1.dp
        Column(
            modifier = Modifier
                .horizontalScroll(tableState.horizontalScrollState)
        ) {
            if (tableConfig.isHeaderVisible) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(AppColors.current.RowBackground.default2)
                        .width(tableWidth)
                ) {
                    repeat(data.columns) { columnIndex ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .width(columnWidths[columnIndex])
                                .height(IntrinsicSize.Min)
                        ) {
                            headerCell(columnIndex)
                        }
                    }
                }
            }
            LazyColumn(
                modifier = modifier, state = tableState.lazyListState
            ) {
                items(data.rows) { rowIndex ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(tableConfig.cellBackground.get(isEven = rowIndex % 2 == 0))
                            .height(data.metaData.getRowHeight(rowIndex, data))
                            .width(tableWidth)
                    )
                    {
                        WSpacer2()
                        repeat(data.columns) { columnIndex ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.width(columnWidths[columnIndex])
                            ) {
                                cell(rowIndex, columnIndex)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun PreviewLazyTable() {
    val lazyListState = rememberLazyListState()
    val horizontalScrollState = rememberScrollState()
    val data = remember {
        val columns = 20
        object : ITableData {
            override val rows: Int = 1000
            override val columns: Int = columns

            override val metaData: ITableMetaData get() = object : ITableMetaData {
                override val columns: Int = columns
                @Composable override fun getHeaderTitle(index: Int): String = "Col:$index"
                override fun getColumnWidth(index: Int, tableData: ITableData): TableCellSize = TableCellSize.Exact(TableCellSize.DefaultMinWidth)
            }

            val items = (0..rows).map { row ->
                (row..row + columns).map { column -> "$column" }
            }

            @Composable
            override fun cell(rowIndex: Int, columnIndex: Int): String = items[rowIndex][columnIndex]
            @Composable
            override fun header(columnIndex: Int) = "Header:${columnIndex}"
        }
    }

    AppTheme {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyTable(
                data,
                TableState.default(lazyListState, horizontalScrollState),
                modifier = Modifier.fillMaxWidth()
            )
            VerticalScrollbar(
                adapter = ScrollbarAdapter(lazyListState),
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
            )
            HorizontalScrollbar(
                adapter = ScrollbarAdapter(horizontalScrollState),
                modifier = Modifier
                    .width(maxWidth)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}




