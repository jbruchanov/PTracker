package com.scurab.ptracker.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

interface ITableMetaData {
    val columns: Int

    @Composable
    fun getHeaderTitle(index: Int): String

    fun getColumnWidth(index: Int, tableData: ITableData): TableCellSize

    fun getRowHeight(index: Int, tableData: ITableData): TableCellSize.Exact = TableCellSize.Exact(24.dp)
}