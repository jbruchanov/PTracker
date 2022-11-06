package com.scurab.ptracker.ui.model

import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.scurab.ptracker.component.compose.StateContainer
import com.scurab.ptracker.ui.AppColors

data class TableConfig(
    override val textColor: Color,
    override val cellBackground: StateContainer<Color>,
    override val isHeaderVisible: Boolean = true
) : ITableConfig {

    companion object {
        @Composable
        fun default() = TableConfig(
            textColor = LocalContentColor.current,
            cellBackground = AppColors.current.RowBackground
        )
    }
}
