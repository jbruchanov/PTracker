package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.model.PriceHistoryChartData
import com.scurab.ptracker.app.model.PriceItemUI
import kotlinx.datetime.LocalDate

//TODO: rename to common
sealed class LineChartUiState {
    object NoPrimaryCurrency : LineChartUiState()
    object Loading : LineChartUiState()
    class Data(
        val chartData: PriceHistoryChartData,
        /**
         * Price data not null only if UI has selected a trading asset (always null in Portfolio stats, there is no asset picker)
         */
        val singleTradingAssetsDailyPrices: Map<LocalDate, PriceItemUI>? = null
    ) : LineChartUiState()

    class Error(val msg: String) : LineChartUiState()
}
