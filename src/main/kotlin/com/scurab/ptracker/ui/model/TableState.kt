package com.scurab.ptracker.ui.model

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable

class TableState(
    override val lazyListState: LazyListState,
    override val horizontalScrollState: ScrollState,
) : ITableState {

    companion object {
        @Composable
        fun default(
            lazyListState: LazyListState = rememberLazyListState(),
            horizontalScrollState: ScrollState = rememberScrollState(),
        ): ITableState = TableState(lazyListState, horizontalScrollState)
    }
}