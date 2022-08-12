package com.scurab.ptracker.ui.stats.portfolio

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.coloredMarketPercentage
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.pieChartData2
import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.StatsChartCalcUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.stats.LineChartUiState
import com.scurab.ptracker.ui.stats.portfolio.PortfolioStatsUiState.Companion.MarketPercentageGroupingThreshold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PortfolioStatsViewModel(
    private val appSettings: AppSettings,
    private val appStateRepository: AppStateRepository,
    private val pricesRepository: PricesRepository,
    private val statsChartCalcUseCase: StatsChartCalcUseCase
) : ViewModel(), StatsEventHandler {

    private var latestData = AppData.Empty
    private var prices = mutableMapOf<Asset, MarketPrice>()
    val uiState = PortfolioStatsUiState().also {
        it.primaryCoin = appSettings.primaryCoin
    }

    init {
        launch {
            appStateRepository.appData.filter { it != AppData.Empty }.collect { data ->
                onNewDataSelected(data)
            }
        }

        launch {
            pricesRepository.wsMarketPrice.collect {
                onMarketPrice(it)
            }
        }
        launch {
            appStateRepository.appData.filter { it != AppData.Empty }.collect { data ->
                try {
                    val primaryCoin = appSettings.primaryCoin
                    if (primaryCoin == null) {
                        uiState.portfolioChartUiState = LineChartUiState.NoPrimaryCurrency
                    } else {
                        uiState.portfolioChartUiState = LineChartUiState.Loading
                        uiState.portfolioChartUiState = LineChartUiState.Data(statsChartCalcUseCase.getLineChartData(data, primaryCoin))
                    }
                } catch (e: Exception) {
                    uiState.portfolioChartUiState = LineChartUiState.Error((e.message ?: "Null exception message") + "\n" + e.stackTraceToString())
                }
            }
        }
    }

    override fun onHoldingsRowClicked(index: Int, onlineHoldingStats: OnlineHoldingStats) {
        uiState.selectedHoldingsAsset = onlineHoldingStats.asset.takeIf { uiState.selectedHoldingsAsset != onlineHoldingStats.asset }
    }

    override fun onFiatRowClicked(fiatCoin: FiatCoin) {
        uiState.selectedHoldingsAsset = Asset("", fiatCoin.item).takeIf { uiState.selectedHoldingsAsset != it }
    }

    private suspend fun onNewDataSelected(data: AppData) {
        latestData = data
        prices.putAll(data.prices)
        recalcData(data, prices, null)
    }

    private suspend fun onMarketPrice(marketPrice: MarketPrice) {
        prices[marketPrice.asset] = marketPrice
        recalcData(latestData, prices, marketPrice)
    }

    private suspend fun recalcData(data: AppData, latestPrices: Map<Asset, MarketPrice>, tick: MarketPrice?) {
        val stats = data.ledgerStats
        val onlineHoldingStats =
            stats.cryptoHoldings.map { (asset, holdings) -> OnlineHoldingStats(now(), holdings, latestPrices[asset] ?: CoinPrice(asset, 0.bd)) }.sortedBy { it.asset }

        val marketPercentage = onlineHoldingStats.coloredMarketPercentage()
        val pieChartData = marketPercentage.pieChartData2(MarketPercentageGroupingThreshold)
        //synchronization against the market ticker, sometimes it added a value
        withContext(Dispatchers.Main) {
            if (tick != null) {
                stats.cryptoHoldings[tick.asset]?.let { holdings ->
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
            uiState.marketPrices = latestPrices
            uiState.marketPercentage = marketPercentage
            uiState.pieChartData = pieChartData
            uiState.coinSumPerExchange = stats.coinSumPerExchange
            uiState.feesPerCoin.putAll(stats.feesPerCoin)
        }
    }
}