package com.scurab.ptracker.ui.model

import androidx.compose.runtime.Composable
import com.scurab.ptracker.app.model.IDataTransformers


interface ITableItem {
    fun getValue(index: Int, render: IDataTransformers): String
}

class ListTableData<T : ITableItem>(
    val items: List<T>,
    override val metaData: ITableMetaData,
    private val render: IDataTransformers,
) : ITableData {
    override val rows: Int = items.size
    override val columns: Int = metaData.columns

    @Composable
    override fun cell(rowIndex: Int, columnIndex: Int): String = items[rowIndex].getValue(columnIndex, render)

    @Composable
    override fun header(columnIndex: Int): String = metaData.getHeaderTitle(columnIndex)
}