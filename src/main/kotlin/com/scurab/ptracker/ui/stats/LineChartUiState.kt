package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.model.PriceHistoryChartData


//TODO: rename to common
sealed class LineChartUiState {
    object NoPrimaryCurrency : LineChartUiState()
    object Loading : LineChartUiState()
    class Data(val chartData: PriceHistoryChartData) : LineChartUiState()
    class Error(val msg: String) : LineChartUiState()
}