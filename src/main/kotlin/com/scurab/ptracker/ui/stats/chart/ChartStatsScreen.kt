package com.scurab.ptracker.ui.stats.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.PriceHistoryChart
import com.scurab.ptracker.ui.common.ToggleButton
import com.scurab.ptracker.ui.common.ToggleButtons
import com.scurab.ptracker.ui.common.VerticalDivider
import com.scurab.ptracker.ui.stats.LineChartUiState
import com.scurab.ptracker.ui.stats.portfolio.statsContentBackground

@Composable
fun ChartStatsScreen(viewModel: ChartStatsViewModel) {
    Column {
        Text(
            text = LocalTexts.current.TradingCoinStats,
            style = AppTheme.TextStyles.Header,
            modifier = Modifier.padding(AppSizes.current.Space2)
        )
        ChartStatsScreen(viewModel.uiState, viewModel)
    }
}

@Composable
private fun ChartStatsScreen(
    uiState: ChartStatsUiState,
    eventHandler: ChartStatsEventHandler
) {
    Box(modifier = Modifier) {
        Column(
            modifier = Modifier
                .padding(AppSizes.current.Padding)
        ) {
            ToggleButtons {
                uiState.assets.forEach { asset ->
                    ToggleButton(text = asset.label,
                        isSelected = asset == uiState.selectedAsset,
                        onClick = { eventHandler.onSelectedAsset(asset) }
                    )
                    VerticalDivider()
                }
            }
            Divider()
            Box(modifier = Modifier) {
                LineChartContent(uiState.chartUiState)
            }
        }
    }
}

@Composable
private fun LineChartContent(chartState: LineChartUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statsContentBackground()
    ) {
        when (chartState) {
            is LineChartUiState.NoPrimaryCurrency -> Text(LocalTexts.current.NoPrimaryCurrencyChart, modifier = Modifier.align(Alignment.Center))
            is LineChartUiState.Error -> Text(chartState.msg, modifier = Modifier.align(Alignment.Center))
            is LineChartUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is LineChartUiState.Data -> PriceHistoryChart(chartState.chartData)
        }
    }
}