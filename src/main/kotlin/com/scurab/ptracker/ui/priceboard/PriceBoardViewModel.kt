package com.scurab.ptracker.ui.priceboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ext.firstIf
import com.scurab.ptracker.ext.firstIndexOf
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.Filter
import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.Transaction
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.model.AssetIcon
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import com.scurab.ptracker.usecase.LoadPriceHistoryUseCase
import com.scurab.ptracker.usecase.PriceBoardDataProcessingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.event.KeyEvent

class PriceBoardUiState(localDensity: Density, grouping: GroupStrategy) {
    var priceBoardState by mutableStateOf(PriceBoardState(emptyList(), localDensity, grouping))
    var assets by mutableStateOf(emptyList<AssetIcon>())
    var hasTradeOnlyFilter by mutableStateOf(true)
}

interface PriceBoardEventDelegate {
    fun onAssetSelected(item: Asset)
    fun onTransactionClicked(item: Transaction, doubleClick: Boolean)
    fun onTransactionHoverChanged(item: Transaction, isHovered: Boolean)
    fun onSpacePressed()
    fun onResetClicked()
    fun onFilterClicked(filter: Filter<*>)
}

class PriceBoardViewModel(
    private val appStateRepository: AppStateRepository,
    private val loadPriceHistoryUseCase: LoadPriceHistoryUseCase,
    private val loadLedgerUseCase: LoadLedgerUseCase,
    private val dataUseCase: PriceBoardDataProcessingUseCase
) : ViewModel(), PriceBoardEventDelegate {

    private val filters = Pair(Filter.ImportantTransactions, Filter.AllTransactions)
    private val grouping = GroupStrategy.Day
    val uiState = PriceBoardUiState(appStateRepository.density.value, grouping)

    //state for merging, 2 different datasources for 1 output
    private val ledger = appStateRepository.ledger
    private val prices = MutableStateFlow(Asset.Empty to emptyList<PriceItem>())

    private var data = PriceBoardDataProcessingUseCase.RawData(Ledger.Empty, Asset.Empty, emptyList())

    init {
        launch {
            appStateRepository.selectedAsset
                .filter { it != Asset.Empty }
                .collect(::loadAsset)
        }
        launch {
            appStateRepository.keyEvents
                .filter { it.nativeKeyCode == KeyEvent.VK_SPACE }
                .collect { onSpacePressed() }
        }

        launch(Dispatchers.Main) {
            ledger.combine(prices) { i1, i2 -> Pair(i1, i2) }.collect { (ledger, pricePair) ->
                val (asset, prices) = pricePair
                data = PriceBoardDataProcessingUseCase.RawData(ledger, asset, prices)
                updateData(data, filters.firstIf(uiState.hasTradeOnlyFilter), true)
            }
        }
    }

    private suspend fun updateData(
        data: PriceBoardDataProcessingUseCase.RawData,
        filter: Filter<Transaction>,
        resetViewport: Boolean
    ) {
        val result = withContext(Dispatchers.IO) { dataUseCase.prepareData(data, filter, grouping) }
        withContext(Dispatchers.Main) {
            with(uiState) {
                assets = result.assets
                hasTradeOnlyFilter = filter == Filter.ImportantTransactions
            }
            with(uiState.priceBoardState) {
                visibleTransactions = result.transactions
                visibleTransactionsPerPriceItem = result.transactionsPerPriceItem
                setItems(data.asset, data.prices, resetViewport)
            }
        }
    }

    private fun loadAsset(asset: Asset) {
        launch(Dispatchers.IO) {
            prices.value = asset to loadPriceHistoryUseCase.load(asset)
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

    override fun onResetClicked() {
        uiState.priceBoardState.apply {
            setViewport(initViewport(), animate = true)
        }
    }

    override fun onTransactionHoverChanged(item: Transaction, isHovered: Boolean) {
        uiState.priceBoardState.pointingTransaction = item.takeIf { isHovered }
    }

    override fun onFilterClicked(filter: Filter<*>) {
        when (filter) {
            is Filter.ImportantTransactions -> {
                launch {
                    updateData(data, filters.firstIf(!uiState.hasTradeOnlyFilter), false)
                }
            }
        }
    }
}