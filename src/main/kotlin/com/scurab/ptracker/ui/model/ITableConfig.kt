package com.scurab.ptracker.ui.model

import androidx.compose.ui.graphics.Color
import com.scurab.ptracker.component.compose.StateContainer

interface ITableConfig {
    val textColor: Color
    val cellBackground: StateContainer<Color>
    val isHeaderVisible: Boolean
}
