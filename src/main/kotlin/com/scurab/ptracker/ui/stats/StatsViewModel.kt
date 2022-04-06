package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.coloredMarketPercentage
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.pieChartData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.StatsCalculatorUseCase
import com.scurab.ptracker.component.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsCalculatorUseCase: StatsCalculatorUseCase,
    private val pricesRepository: PricesRepository
) : ViewModel(), StatsEventHandler {

    private var latestLedger: Ledger? = null
    private var ledgerStats = LedgerStats.Empty
    private var prices = mutableMapOf<Asset, MarketPrice>()
    val uiState = StatsUiState()

    init {
        launch {
            appStateRepository.ledger
                .filter { it != Ledger.Empty }
                .collect { ledger ->
                    onNewLedgerSelected(ledger)
                }
        }

        launch {
            pricesRepository.wsMarketPrice.collect {
                onMarketPrice(it)
            }
        }
    }

    override fun onHoldingsRowClicked(index: Int, onlineHoldingStats: OnlineHoldingStats) {
        uiState.selectedHoldingsAsset = onlineHoldingStats.asset.takeIf { uiState.selectedHoldingsAsset != onlineHoldingStats.asset }
    }

    private suspend fun onNewLedgerSelected(ledger: Ledger) {
        latestLedger = ledger
        prices.putAll(pricesRepository.getPrices(ledger.assets).associateBy { it.asset })
        ledgerStats = statsCalculatorUseCase.calculateStats(ledger, Filter.AllTransactions)
        recalcData(ledgerStats, prices, null)
    }

    private suspend fun onMarketPrice(marketPrice: MarketPrice) {
        prices[marketPrice.asset] = marketPrice
        recalcData(ledgerStats, prices, marketPrice)
    }

    private suspend fun recalcData(ledgerStats: LedgerStats, prices: Map<Asset, MarketPrice>, tick: MarketPrice?) {
        val onlineHoldingStats = ledgerStats.holdinds
            .map { (asset, holdings) -> OnlineHoldingStats(now(), holdings, prices[asset] ?: CoinPrice(asset, 0.bd)) }
            .sortedBy { it.asset }

        val colorGroupingThreshold = 0.015f
        val marketPercentage = onlineHoldingStats.coloredMarketPercentage(colorGroupingThreshold)
        val pieChartData = marketPercentage.pieChartData(colorGroupingThreshold)
        //synchronization against the market ticker, sometimes it added a value
        withContext(Dispatchers.Main) {
            if (tick != null) {
                ledgerStats.holdinds[tick.asset]?.let { holdings ->
                    val indexOfFirst = uiState.holdings.indexOfFirst { it.asset == tick.asset }
                    //missing asset might happen in case of changing ledgers
                    if (indexOfFirst != -1) {
                        uiState.holdings[indexOfFirst] = holdings.realtimeStats(tick)
                    }
                }
            } else {
                uiState.holdings.clear()
                uiState.holdings.addAll(onlineHoldingStats)
            }
            uiState.marketPercentage = marketPercentage
            uiState.pieChartData = pieChartData
        }
    }
}