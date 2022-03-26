package com.scurab.ptracker.ui.priceboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.usecase.LoadDataUseCase
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PriceBoardUiState(localDensity: Density) {
    var priceBoardState by mutableStateOf(PriceBoardState(emptyList(), localDensity))
    var assets by mutableStateOf(emptyList<Asset>())
}

class PriceBoardViewModel(
    private val appStateRepository: AppStateRepository,
    private val loadDataUseCase: LoadDataUseCase,
    private val loadLedgerUseCase: LoadLedgerUseCase
) : ViewModel() {

    val uiState = PriceBoardUiState(Density(1f))
    private val ledger = MutableStateFlow(Ledger.Empty)
    private val prices = MutableStateFlow(Asset.Empty to emptyList<PriceItem>())

    init {
        launch {
            appStateRepository.selectedAsset.collect(::loadAsset)
        }
        launch(Dispatchers.IO) {
            ledger.value = runCatching { loadLedgerUseCase.load(File("data/output.xlsx")) }.getOrDefault(Ledger.Empty)
        }
        launch(Dispatchers.IO) {
            ledger.combine(prices) { i1, i2 -> Pair(i1, i2) }
                .collect { (ledger, pricePair) ->
                    val (asset, prices) = pricePair
                    val transactions = ledger.getTransactions(asset)
                    withContext(Dispatchers.Main) {
                        uiState.assets = ledger.assets
                        uiState.priceBoardState.ledger = ledger
                        uiState.priceBoardState.visibleTransactions = transactions
                        uiState.priceBoardState.setItemsAndInitViewPort(asset, prices)
                    }
                }
        }
    }

    private fun loadAsset(asset: Asset) {
        launch(Dispatchers.IO) {
            prices.value = asset to loadDataUseCase.loadData(asset)
        }
    }

    fun onAssetSelected(item: Asset) {
        appStateRepository.setSelectedAsset(item)
    }
}