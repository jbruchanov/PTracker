package com.scurab.ptracker.ui.priceboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ext.firstIndexOf
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.Transaction
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.usecase.LoadDataUseCase
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import com.scurab.ptracker.usecase.UpdateTransactionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PriceBoardUiState(localDensity: Density, grouping: GroupStrategy) {
    var priceBoardState by mutableStateOf(PriceBoardState(emptyList(), localDensity, grouping))
    var assets by mutableStateOf(emptyList<Asset>())
}

interface PriceBoardEventDelegate {
    fun onAssetSelected(item: Asset)
    fun onTransactionClicked(item: Transaction, doubleClick: Boolean)
    fun onSpacePressed()
}

class PriceBoardViewModel(
    private val appStateRepository: AppStateRepository,
    private val loadDataUseCase: LoadDataUseCase,
    private val loadLedgerUseCase: LoadLedgerUseCase,
    private val updateTransactionsUseCase: UpdateTransactionsUseCase
) : ViewModel(), PriceBoardEventDelegate {

    private val grouping = GroupStrategy.Day
    val uiState = PriceBoardUiState(Density(1f), grouping)
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
            ledger.combine(prices) { i1, i2 -> Pair(i1, i2) }.collect { (ledger, pricePair) ->
                val (asset, prices) = pricePair
                val transactions = ledger.getTransactions(asset)
                updateTransactionsUseCase.fillPriceItems(transactions, prices, grouping)
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

    override fun onAssetSelected(item: Asset) {
        appStateRepository.setSelectedAsset(item)
    }

    override fun onTransactionClicked(item: Transaction, doubleClick: Boolean) {
        val state = uiState.priceBoardState
        launch {
            val priceItem = requireNotNull(item.priceItem) { "item.priceItem:${item}" }
            if (doubleClick || !priceItem.isVisible(state)) {
                withContext(Dispatchers.Main) {
                    state.setViewport(state.initViewport(priceItemIndex = priceItem.index + 1, alignCenter = true), animate = true)
                }
            }
        }
        state.highlightTransaction = item
    }

    override fun onSpacePressed() {
        val boardState = uiState.priceBoardState
        boardState.pointedPriceItem?.let { priceItem ->
            val index = boardState.visibleTransactions.firstIndexOf(priceItem, boardState.grouping)
            if (index >= 0) {
                boardState.scrollToTransactionIndex = index
            }
        }
    }
}