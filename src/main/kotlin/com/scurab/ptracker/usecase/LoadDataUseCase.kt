package com.scurab.ptracker.usecase

import com.scurab.ptracker.model.AppData
import com.scurab.ptracker.model.Filter
import com.scurab.ptracker.repository.AppSettings
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.repository.PricesRepository
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
        val historyDef = async(Dispatchers.IO) { loadPriceHistoryUseCase.loadAll(ledger.assets) }
        val iconsDef = async(Dispatchers.IO) { loadIconsUseCase.loadIcons(ledger.assets) }
        val statsDef = async(Dispatchers.IO) { statsCalculatorUseCase.calculateStats(ledger, Filter.AllTransactions) }
        val pricesDef = async(Dispatchers.IO) { pricesRepository.getPrices(ledger.assets) }

        val history = historyDef.await().mapValues { it.value.getOrNull() }.mapValues { it.value ?: emptyList() }
        val stats = statsDef.await()
        val prices = pricesDef.await()
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
            pricesRepository.subscribeWsRandomPrices(
                data.ledgerStats.assetsByExchange
            )
        }
    }
}