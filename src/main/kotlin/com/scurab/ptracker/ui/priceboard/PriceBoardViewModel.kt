package com.scurab.ptracker.ui.priceboard

import androidx.compose.ui.unit.Density
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.usecase.LoadDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed class PriceBoardUiState {
    object NoAssetSelected : PriceBoardUiState()
    class Data(
        val priceBoardState: PriceBoardState,
        val pairs: List<String>
    ) : PriceBoardUiState()
}

class PriceBoardViewModel(
    private val appStateRepository: AppStateRepository,
    private val loadDataUseCase: LoadDataUseCase
) : ViewModel() {

    private val crypto = listOf("BTC", "ETH", "ADA", "LTC", "SOL")
    private val fiat = listOf("GBP", "USD")
    private val pairs = crypto.map { c -> fiat.map { f -> "$c-$f" } }.flatten()

    private val _uiState = MutableStateFlow<PriceBoardUiState>(PriceBoardUiState.NoAssetSelected)
    val uiState = _uiState.asStateFlow()

    init {
        launch {
            appStateRepository.selectedAsset.collect(::onAssetSelected)
        }
    }

    private fun onAssetSelected(item: String) {
        launch {
            val items = loadDataUseCase.loadData(item)
            val data = (_uiState.value as? PriceBoardUiState.Data)
            if (data != null) {
                data.priceBoardState.setItemsAndInitViewPort(items)
            } else {
                _uiState.emit(
                    PriceBoardUiState.Data(
                        PriceBoardState(items, Density(1f)),
                        pairs
                    )
                )
            }
        }
    }

    fun onPairSelected(item: String) {
        appStateRepository.setSelectedAsset(item)
    }
}