package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ext.sign
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.CoinPrice
import com.scurab.ptracker.model.ExchangeWallet
import com.scurab.ptracker.model.Filter
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.LedgerStats
import com.scurab.ptracker.model.MarketPrice
import com.scurab.ptracker.net.model.CryptoCompareWssResponse
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.repository.PricesRepository
import com.scurab.ptracker.usecase.StatsCalculatorUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.delayEach
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

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
                .collect {
                    latestLedger = it
                    ledgerStats = statsCalculatorUseCase.calculateStats(it, Filter.AllTransactions)
                    val prices = pricesRepository.getPrices(it.assets)
                    startFlow(ledgerStats.assetsByExchange)
                    prices.forEach(::onMarketPrice)
                }
        }
    }

    private var pricesStream: Job? = null

    private fun startFlow(assets: Map<ExchangeWallet, List<Asset>>) {
        require(pricesStream == null)
        pricesStream = launch {
            pricesRepository.flowPrices(assets)
                .consumeEach {
                    when (it) {
                        is MarketPrice -> onMarketPrice(it)
                    }
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