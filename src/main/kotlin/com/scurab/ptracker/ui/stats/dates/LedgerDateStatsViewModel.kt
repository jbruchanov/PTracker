package com.scurab.ptracker.ui.stats.dates

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.IDataTransformers
import com.scurab.ptracker.app.model.Tuple
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.usecase.StatsDatesUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.model.ITableData
import com.scurab.ptracker.ui.model.ListTableData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class LedgerDateStatsUiState {
    var selectedGroupingKey by mutableStateOf(DateGrouping.Month)
    var selectedAsset by mutableStateOf<Asset?>(null)
    var assets by mutableStateOf(emptyList<Asset>())
    var coins by mutableStateOf(emptyList<String>())
    var tableData by mutableStateOf(ITableData.Empty)
}

interface LedgerDateStatsEventHandler {
    fun onSelectedGrouping(grouping: DateGrouping)
    fun onSelectedAsset(asset: Asset)
    fun onSelectedCoin(coin: String)
}

class LedgerDateStatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsUseCase: StatsDatesUseCase,
    private val dataTransformers: IDataTransformers
) : ViewModel(), LedgerDateStatsEventHandler {
    val uiState = LedgerDateStatsUiState()

    private val flowGrouping = MutableStateFlow(uiState.selectedGroupingKey)
    private val flowAsset = MutableStateFlow(uiState.selectedAsset)

    init {
        launch {
            appStateRepository.appData
                .combine(flowGrouping, ::Pair)
                .combine(flowAsset) { appDataAndGrouping, selectedAsset ->
                    val (appData, grouping) = appDataAndGrouping
                    val asset = selectedAsset?.takeIf {
                        appData.ledger.assetsTradings.contains(it) ||
                                (it.isSingleCoinAsset && appData.ledger.coins.contains(it.coin1))
                    }
                    Tuple(appData, grouping, statsUseCase.getStats(appData.ledger, grouping, asset), asset)
                }
                .collectLatest { (appData, grouping, tableData, asset) ->
                    uiState.selectedAsset = asset
                    uiState.assets = appData.ledger.assetsTradings
                    uiState.coins = appData.ledger.coins
                    uiState.selectedGroupingKey = grouping
                    uiState.tableData = ListTableData(tableData, StatsDatesUseCase.StatsItem, dataTransformers)
                }
        }
    }

    override fun onSelectedGrouping(grouping: DateGrouping) {
        flowGrouping.tryEmit(grouping)
    }

    override fun onSelectedAsset(asset: Asset) {
        flowAsset.tryEmit(asset.takeIf { uiState.selectedAsset != asset })
    }

    override fun onSelectedCoin(coin: String) {
        val asset = Asset(coin, "")
        flowAsset.tryEmit(asset.takeIf { uiState.selectedAsset != asset })
    }
}