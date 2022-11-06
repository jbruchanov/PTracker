package com.scurab.ptracker.ui.model

import androidx.compose.runtime.Composable

interface ITableData {
    val rows: Int
    val columns: Int
    val metaData: ITableMetaData

    @Composable
    operator fun get(rowIndex: Int, columnIndex: Int): String = cell(rowIndex, columnIndex)

    @Composable
    fun cell(rowIndex: Int, columnIndex: Int): String

    @Composable
    fun header(columnIndex: Int): String

    companion object {
        private val EmptyMetaData = object : ITableMetaData {
            override val columns: Int = 0

            @Composable
            override fun getHeaderTitle(index: Int): String = throw IllegalStateException("Empty data has no headers")

            override fun getColumnWidth(index: Int, tableData: ITableData) = throw IllegalStateException("Empty data")
        }

        val Empty = object : ITableData {
            override val rows: Int = 0
            override val columns: Int = 0
            override val metaData: ITableMetaData = EmptyMetaData

            @Composable
            override fun cell(rowIndex: Int, columnIndex: Int): Nothing = throw IllegalStateException("Empty data has no cells")

            @Composable
            override fun header(columnIndex: Int): Nothing = throw IllegalStateException("Empty data has no headers")
        }
    }
}
