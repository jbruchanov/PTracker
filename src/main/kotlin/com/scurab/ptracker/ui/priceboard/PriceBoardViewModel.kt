package com.scurab.ptracker.ui.priceboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.usecase.LoadDataUseCase
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PriceBoardUiState(localDensity: Density) {
    var priceBoardState by mutableStateOf(PriceBoardState(emptyList(), localDensity))
    var ledger by mutableStateOf(Ledger.Empty)
    val assets get() = ledger.assets
}

class PriceBoardViewModel(
    private val appStateRepository: AppStateRepository,
    private val loadDataUseCase: LoadDataUseCase,
    private val loadLedgerUseCase: LoadLedgerUseCase
) : ViewModel() {

    val uiState = PriceBoardUiState(Density(1f))

    init {
        launch {
            appStateRepository.selectedAsset.collect(::loadAsset)
        }
        launch(Dispatchers.IO) {
            val ledger = runCatching { loadLedgerUseCase.load(File("data/output.xlsx")) }.getOrDefault(Ledger.Empty)
            withContext(Dispatchers.Main) {
                uiState.ledger = ledger
                uiState.priceBoardState.ledger = ledger
            }
        }
    }

    private fun loadAsset(asset: Asset) {
        launch(Dispatchers.IO) {
            val loadData = loadDataUseCase.loadData(asset)
            withContext(Dispatchers.Main) {
                uiState.priceBoardState.setItemsAndInitViewPort(asset, loadData)
            }
        }
    }

    fun onAssetSelected(item: Asset) {
        appStateRepository.setSelectedAsset(item)
    }
}