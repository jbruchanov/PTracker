package com.scurab.ptracker.ui.model

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState

interface ITableState {
    val lazyListState: LazyListState
    val horizontalScrollState: ScrollState
}
