package com.scurab.ptracker.ui.stats.chart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.model.Tuple4
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.StatsChartCalcUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.component.PriceTickingComponent
import com.scurab.ptracker.ui.stats.GroupingAssetComponent
import com.scurab.ptracker.ui.stats.LineChartUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChartStatsUiState {
    var selectedGroupingKey by mutableStateOf(DateGrouping.Day)
    var selectedAsset by mutableStateOf<Asset?>(null)
    var assets by mutableStateOf(emptyList<Asset>())
    var chartUiState by mutableStateOf<LineChartUiState>(LineChartUiState.Loading)
    var historyDetailsVisible by mutableStateOf(false)
    val prices = mutableStateMapOf<Asset, MarketPrice>()
}

interface ChartStatsEventHandler {
    fun onSelectedAsset(asset: Asset)
    fun onSelectedGrouping(grouping: DateGrouping)
    fun onExpandCollapseHistoryClick()
}

class ChartStatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsChartCalcUseCase: StatsChartCalcUseCase,
    private val appSettings: AppSettings,
    pricesRepository: PricesRepository
) : ViewModel(), ChartStatsEventHandler,
    GroupingAssetComponent by GroupingAssetComponent.Default(DateGrouping.Day, null),
    PriceTickingComponent by PriceTickingComponent.Default(pricesRepository) {

    val uiState = ChartStatsUiState()

    init {
        appSettings.statsSelectedAsset?.let {
            uiState.selectedAsset = it
            tryEmitAsset(it)
        }
        launch(Dispatchers.IO) {
            appStateRepository.appData.combineWithGroupingAsset()
                .map { (appData, dateGrouping, asset) ->
                    uiState.assets = appData.ledger.assetsTradings
                    uiState.selectedGroupingKey = dateGrouping
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
                                    selectedFiat,
                                    dateGrouping
                                ),
                                appData.historyPrices[asset]
                                    .takeIf { dateGrouping == DateGrouping.Day }
                                    ?.associateBy { it.dateTime.date }
                            )
                        }
                    } catch (e: Exception) {
                        LineChartUiState.Error((e.message ?: "Null exception message") + "\n" + e.stackTraceToString())
                    }
                    Tuple4(appData, dateGrouping, asset, lineChartState)
                }
                .collectLatest { (appData, dateGrouping, asset, lineChartState) ->
                    uiState.selectedAsset = asset
                    uiState.assets = appData.ledger.assetsTradings
                    uiState.chartUiState = lineChartState
                    uiState.selectedGroupingKey = dateGrouping
                }
        }

        startPriceObserver(uiState.prices)
    }

    override fun onExpandCollapseHistoryClick() {
        uiState.historyDetailsVisible = !uiState.historyDetailsVisible
    }

    override fun onSelectedAsset(asset: Asset) {
        val newAsset = asset.takeIf { uiState.selectedAsset != asset }
        tryEmitAsset(newAsset)
        appSettings.statsSelectedAsset = newAsset
    }

    override fun onSelectedGrouping(grouping: DateGrouping) {
        if (uiState.selectedGroupingKey != grouping) {
            tryEmitGrouping(grouping)
        }
    }
}