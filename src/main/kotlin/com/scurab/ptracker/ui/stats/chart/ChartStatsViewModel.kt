package com.scurab.ptracker.ui.stats.chart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DataFilter
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.PriceItemUI
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.model.Tuple5
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
import kotlinx.datetime.toKotlinLocalDate
import java.time.LocalDate

class ChartStatsUiState {
    var selectedGroupingKey by mutableStateOf(DateGrouping.Day)
    var selectedAsset by mutableStateOf<Asset?>(null)
    var selectedDataFilter by mutableStateOf(DataFilter.Last6Months)
    var assets by mutableStateOf(emptyList<Asset>())
    var chartUiState by mutableStateOf<LineChartUiState>(LineChartUiState.Loading)
    var historyDetailsVisible by mutableStateOf(false)
    val prices = mutableStateMapOf<Asset, MarketPrice>()
}

interface ChartStatsEventHandler {
    fun onSelectedAsset(asset: Asset)
    fun onSelectedGrouping(grouping: DateGrouping)
    fun onExpandCollapseHistoryClick()
    fun onSelectedFilter(filter: DataFilter)
}

class ChartStatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsChartCalcUseCase: StatsChartCalcUseCase,
    private val appSettings: AppSettings,
    private val pricesRepository: PricesRepository
) : ViewModel(),
    ChartStatsEventHandler,
    GroupingAssetComponent by GroupingAssetComponent.Default(DateGrouping.Day, asset = null, filter = DataFilter.Last6Months),
    PriceTickingComponent by PriceTickingComponent.Default(pricesRepository) {

    val uiState = ChartStatsUiState()

    init {
        appSettings.statsSelectedAsset?.let {
            uiState.selectedAsset = it
            tryEmitAsset(it)
        }
        launch(Dispatchers.IO) {
            appStateRepository.appData.combineWithGroupingAsset()
                .map { (appData, dateGrouping, asset, dataFilter) ->
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
                                    merge(appData.historyPrices, pricesRepository.latestPrices[asset]),
                                    selectedFiat,
                                    dateGrouping,
                                    dataFilter
                                ),
                                appData.historyPrices[asset]
                                    .takeIf { dateGrouping == DateGrouping.Day }
                                    ?.associateBy { it.dateTime.date }
                            )
                        }
                    } catch (e: Exception) {
                        LineChartUiState.Error((e.message ?: "Null exception message") + "\n" + e.stackTraceToString())
                    }
                    Tuple5(appData, dateGrouping, asset, lineChartState, dataFilter)
                }
                .collectLatest { (appData, dateGrouping, asset, lineChartState, dataFilter) ->
                    uiState.selectedAsset = asset
                    uiState.assets = appData.ledger.assetsTradings
                    uiState.chartUiState = lineChartState
                    uiState.selectedGroupingKey = dateGrouping
                    uiState.selectedDataFilter = dataFilter
                }
        }

        startPriceObserver(uiState.prices)
    }

    private fun merge(historyPrices: Map<Asset, List<PriceItemUI>>, latestPriceForAsset: MarketPrice?): Map<Asset, List<PriceItemUI>> {
        val asset = latestPriceForAsset?.asset ?: return historyPrices
        val items = historyPrices[asset] ?: return historyPrices
        if (items.isEmpty()) return historyPrices

        val lastPrice = items.last()
        return if (lastPrice.dateTime.date <= LocalDate.now().toKotlinLocalDate()) {
            val result = historyPrices.toMutableMap()
            result[asset] = items.toMutableList()
                .also { it[items.size - 1] = lastPrice.copy(item = lastPrice.withCurrentMarketPrice(latestPriceForAsset)) }
            result
        } else {
            historyPrices
        }
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

    override fun onSelectedFilter(filter: DataFilter) {
        uiState.selectedDataFilter = filter
        tryEmitFilter(filter)
    }
}
