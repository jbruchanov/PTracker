package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.withPrimaryCoin
import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File

class LoadDataUseCase(
    private val loadLedgerUseCase: LoadLedgerUseCase,
    private val loadPriceHistoryUseCase: LoadPriceHistoryUseCase,
    private val loadIconsUseCase: LoadIconsUseCase,
    private val statsCalculatorUseCase: StatsCalculatorUseCase,
    private val appSettings: AppSettings,
    private val appStateRepository: AppStateRepository,
    private val pricesRepository: PricesRepository
) {

    suspend fun loadAllData(ledgerUri: String) = coroutineScope {
        //TODO: global uri
        val ledgerFile = File(ledgerUri)
        val ledger = loadLedgerUseCase.load(ledgerFile)
        val assets = ledger.assetsForPrices.withPrimaryCoin(appSettings.primaryCoin)
        val prices = pricesRepository.getPrices(assets).associateBy { it.asset }
        val historyDef = async(Dispatchers.IO) { loadPriceHistoryUseCase.loadAll(assets) }
        val iconsDef = async(Dispatchers.IO) { loadIconsUseCase.loadIcons(assets) }
        val statsDef = async(Dispatchers.IO) { statsCalculatorUseCase.calculateStats(ledger, Filter.AllTransactions, prices) }

        val history = historyDef.await().mapValues { it.value.getOrNull() }.mapValues { it.value ?: emptyList() }
        val stats = statsDef.await()
        iconsDef.await()

        return@coroutineScope AppData(
            ledger, prices, history, stats
        )
    }

    suspend fun loadAndSetAllData(ledgerUri: String) {
        val data = loadAllData(ledgerUri)
        appSettings.latestLedger = ledgerUri
        appStateRepository.setAppData(data)
        if (appSettings.cryptoCompareApiKey != null) {
//            pricesRepository.subscribeWs(
            pricesRepository.subscribeWsRandomPrices(
                data.ledgerStats.assetsByExchange
            )
        }
    }
}