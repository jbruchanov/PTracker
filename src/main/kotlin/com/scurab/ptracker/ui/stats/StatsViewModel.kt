package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.coloredMarketPercentage
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.pieChartData2
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.StatsCalculatorUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.stats.StatsUiState.Companion.MarketPercentageGroupingThreshold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatsViewModel(
    private val appSettings: AppSettings,
    private val appStateRepository: AppStateRepository,
    private val statsCalculatorUseCase: StatsCalculatorUseCase,
    private val pricesRepository: PricesRepository
) : ViewModel(), StatsEventHandler {

    private var latestLedger: Ledger? = null
    private var ledgerStats = LedgerStats.Empty
    private var prices = mutableMapOf<Asset, MarketPrice>()
    val uiState = StatsUiState().also {
        it.primaryCoin = appSettings.primaryCoin
    }

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

    override fun onFiatRowClicked(fiatCoin: FiatCoin) {
        uiState.selectedHoldingsAsset = Asset("", fiatCoin.item).takeIf { uiState.selectedHoldingsAsset != it }
    }

    private suspend fun onNewLedgerSelected(ledger: Ledger) {
        latestLedger = ledger
        val actualPrices = pricesRepository.getPrices(ledger.assets)
        prices.putAll(actualPrices.associateBy { it.asset })
        ledgerStats = statsCalculatorUseCase.calculateStats(ledger, Filter.AllTransactions, actualPrices)
        recalcData(ledgerStats, prices, null)
    }

    private suspend fun onMarketPrice(marketPrice: MarketPrice) {
        prices[marketPrice.asset] = marketPrice
        recalcData(ledgerStats, prices, marketPrice)
    }

    private suspend fun recalcData(ledgerStats: LedgerStats, prices: Map<Asset, MarketPrice>, tick: MarketPrice?) {
        val onlineHoldingStats = ledgerStats.cryptoHoldings
            .map { (asset, holdings) -> OnlineHoldingStats(now(), holdings, prices[asset] ?: CoinPrice(asset, 0.bd)) }
            .sortedBy { it.asset }

        val marketPercentage = onlineHoldingStats.coloredMarketPercentage()
        val pieChartData = marketPercentage.pieChartData2(MarketPercentageGroupingThreshold)
        //synchronization against the market ticker, sometimes it added a value
        withContext(Dispatchers.Main) {
            if (tick != null) {
                ledgerStats.cryptoHoldings[tick.asset]?.let { holdings ->
                    val indexOfFirst = uiState.cryptoHoldings.indexOfFirst { it.asset == tick.asset }
                    //missing asset might happen in case of changing ledgers
                    if (indexOfFirst != -1) {
                        uiState.cryptoHoldings[indexOfFirst] = holdings.realtimeStats(tick)
                    }
                }
            } else {
                uiState.cryptoHoldings.clear()
                uiState.cryptoHoldings.addAll(onlineHoldingStats)
            }
            uiState.marketPercentage = marketPercentage
            uiState.pieChartData = pieChartData
            uiState.coinSumPerExchange = ledgerStats.coinSumPerExchange
            uiState.feesPerCoin.putAll(ledgerStats.feesPerCoin)
        }
    }
}