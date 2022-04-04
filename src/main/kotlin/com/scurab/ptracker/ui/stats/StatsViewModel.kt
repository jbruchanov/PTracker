package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.StatsCalculatorUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.OnlineHoldingStats
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class StatsViewModel(
    private val appStateRepository: AppStateRepository,
    private val statsCalculatorUseCase: StatsCalculatorUseCase,
    private val pricesRepository: PricesRepository
) : ViewModel(), StatsEventHandler {

    private var latestLedger: Ledger? = null
    private var ledgerStats = LedgerStats.Empty
    val uiState = StatsUiState()

    init {
        launch {
            appStateRepository.ledger
                .filter { it != Ledger.Empty }
                .collect { ledger ->
                    uiState.holdings.clear()
                    latestLedger = ledger
                    ledgerStats = statsCalculatorUseCase.calculateStats(ledger, Filter.AllTransactions)
                    val prices = pricesRepository.getPrices(ledger.assets).associateBy { it.asset }
                    val onlineHoldingStats = ledgerStats.holdinds
                        .map { (asset, holdings) -> OnlineHoldingStats(now(), holdings, prices[asset] ?: CoinPrice(asset, 0.bd)) }
                        .sortedBy { it.asset }
                    uiState.holdings.addAll(onlineHoldingStats)
                }
        }

        launch {
            pricesRepository.wsMarketPrice.collect {
                onMarketPrice(it)
            }
        }
    }

    private fun onMarketPrice(marketPrice: MarketPrice) {
        val asset = marketPrice.asset
        ledgerStats.holdinds[asset]?.let { holdings ->
            val indexOfFirst = uiState.holdings.indexOfFirst { it.asset == asset }
            if (indexOfFirst == -1) {
                uiState.holdings.add(holdings.realtimeStats(marketPrice))
            } else {
                uiState.holdings[indexOfFirst] = holdings.realtimeStats(marketPrice)
            }
        }
    }
}