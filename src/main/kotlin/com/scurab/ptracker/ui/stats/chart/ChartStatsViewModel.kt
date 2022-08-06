package com.scurab.ptracker.ui.stats.chart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.model.Tuple4
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.usecase.StatsChartCalcUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.stats.GroupingAssetComponent
import com.scurab.ptracker.ui.stats.LineChartUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChartStatsUiState {
    var selectedAsset by mutableStateOf<Asset?>(null)
    var assets by mutableStateOf(emptyList<Asset>())
    var chartUiState by mutableStateOf<LineChartUiState>(LineChartUiState.Loading)
}

interface ChartStatsEventHandler {
    fun onSelectedAsset(asset: Asset)
}

class ChartStatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsChartCalcUseCase: StatsChartCalcUseCase,
    private val appSettings: AppSettings
) : ViewModel(), ChartStatsEventHandler,
    GroupingAssetComponent by GroupingAssetComponent.Default(DateGrouping.NoGrouping, null) {

    val uiState = ChartStatsUiState()

    init {
        launch(Dispatchers.IO) {
            appStateRepository.appData.combineWithGroupingAsset()
                .map { (appData, grouping, asset) ->
                    uiState.assets = appData.ledger.assetsTradings
                    val lineChartState = try {
                        val primaryCoin = appSettings.primaryCoin
                        val selectedFiat = asset?.fiatCoinOrNull()?.item ?: primaryCoin
                        if (selectedFiat == null) {
                            LineChartUiState.NoPrimaryCurrency
                        } else {
                            uiState.chartUiState = LineChartUiState.Loading
                            LineChartUiState.Data(
                                statsChartCalcUseCase.getLineChartData(
                                    appData.ledger.items.filter { asset == null || (it is Transaction.Trade && it.hasAsset(asset)) },
                                    appData.historyPrices,
                                    selectedFiat
                                )
                            )
                        }
                    } catch (e: Exception) {
                        LineChartUiState.Error((e.message ?: "Null exception message") + "\n" + e.stackTraceToString())
                    }
                    Tuple4(appData, grouping, asset, lineChartState)
                }
                .collectLatest { (appData, _, asset, lineChartState) ->
                    uiState.selectedAsset = asset
                    uiState.assets = appData.ledger.assetsTradings
                    uiState.chartUiState = lineChartState
                }
        }
    }

    override fun onSelectedAsset(asset: Asset) : Unit {
        tryEmitAsset(asset.takeIf { uiState.selectedAsset != asset })
    }
}