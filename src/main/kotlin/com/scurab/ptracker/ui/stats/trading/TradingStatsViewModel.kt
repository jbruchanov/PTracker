package com.scurab.ptracker.ui.stats.trading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.IDataTransformers
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.Tuple4
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.StatsDatesUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.component.PriceTickingComponent
import com.scurab.ptracker.ui.model.ITableData
import com.scurab.ptracker.ui.model.ListTableData
import com.scurab.ptracker.ui.stats.GroupingAssetComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class TradingStatsStatsUiState {
    var selectedGroupingKey by mutableStateOf(DateGrouping.Month)
    var selectedAsset by mutableStateOf<Asset?>(null)
    var assets by mutableStateOf(emptyList<Asset>())
    var coins by mutableStateOf(emptyList<String>())
    var tableData by mutableStateOf(ITableData.Empty)
    val prices = mutableStateMapOf<Asset, MarketPrice>()
}

interface TradingStatsEventHandler {
    fun onSelectedGrouping(grouping: DateGrouping)
    fun onSelectedAsset(asset: Asset)
    fun onSelectedCoin(coin: String)
}

class TradingStatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsUseCase: StatsDatesUseCase,
    private val dataTransformers: IDataTransformers,
    private val appSettings: AppSettings,
    pricesRepository: PricesRepository
) : ViewModel(), TradingStatsEventHandler,
    GroupingAssetComponent by GroupingAssetComponent.Default(DateGrouping.Month, null),
    PriceTickingComponent by PriceTickingComponent.Default(pricesRepository) {

    val uiState = TradingStatsStatsUiState()

    init {
        appSettings.statsSelectedAsset?.let {
            uiState.selectedAsset = it
            tryEmitAsset(it)
        }
        launch(Dispatchers.IO) {
            appStateRepository.appData
                .combineWithGroupingAsset()
                .map { (appData, grouping, asset) ->
                    Tuple4(appData, grouping, statsUseCase.getStats(appData.ledger, grouping, asset), asset)
                }
                .collectLatest { (appData, grouping, tableData, asset) ->
                    uiState.selectedAsset = asset
                    uiState.assets = appData.ledger.assetsTradings
                    uiState.coins = appData.ledger.coins
                    uiState.selectedGroupingKey = grouping
                    uiState.tableData = ListTableData(tableData, StatsDatesUseCase.StatsItem.tableMetaData(grouping, asset), dataTransformers)
                }
        }

        startPriceObserver(uiState.prices)
    }

    override fun onSelectedGrouping(grouping: DateGrouping) {
        tryEmitGrouping(grouping)
    }

    override fun onSelectedAsset(asset: Asset) {
        val newAsset = asset.takeIf { uiState.selectedAsset != asset }
        tryEmitAsset(newAsset)
        appSettings.statsSelectedAsset = newAsset
    }

    override fun onSelectedCoin(coin: String) {
        val asset = Asset(coin, "")
        tryEmitAsset(asset.takeIf { uiState.selectedAsset != asset })
    }
}