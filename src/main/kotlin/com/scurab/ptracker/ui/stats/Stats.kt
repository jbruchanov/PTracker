package com.scurab.ptracker.ui.stats

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.component.util.mock
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.common.PieChart
import com.scurab.ptracker.ui.common.PieChartSegment
import com.scurab.ptracker.ui.common.WSpacer4
import stub.StubData

class StatsUiState {
    var isLoading by mutableStateOf(false)
    var holdings = mutableStateListOf<OnlineHoldingStats>()
    var pieChartData = mutableStateListOf<PieChartSegment>()

    companion object {
        val IconToGrayscaleDelay = 60_000L
    }
}


interface StatsEventHandler {

}

@Composable
fun StatsScreen(vm: StatsViewModel) {
    Row(modifier = Modifier) {
        Box(
            modifier = Modifier.padding(AppSizes.current.Space).weight(1f)
        ) {
            StatsScreen(vm.uiState, vm)
        }
    }
}

@Composable
private fun StatsScreen(state: StatsUiState, event: StatsEventHandler) {
    Box {
        val vScrollState = rememberScrollState()
        Row(modifier = Modifier.verticalScroll(vScrollState)) {
            Holdings(state, event)
            WSpacer4()
        }
        VerticalScrollbar(rememberScrollbarAdapter(vScrollState), modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun StatsPieChart(data: List<PieChartSegment>) {
    PieChart(data)
}


@Preview
@Composable
private fun PreviewStatsScreen() {
    AppTheme {
        val uiState = StatsUiState().apply {
            this.holdings.addAll(StubData.onlineStubHoldings())
            this.pieChartData.addAll(StubData.pieChartData())
        }
        StatsScreen(uiState, StatsEventHandler::class.mock())
    }
}