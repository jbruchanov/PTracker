package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.repository.AppSettingsJsonRepository
import com.scurab.ptracker.app.repository.MemoryAppSettings
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.app.util.LedgerParsingProcessor
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
internal class StatsCalculatorUseCaseTest {

    val settings = MemoryAppSettings().apply { primaryCoin = "GBP" }

    @Test
    fun man() {
        runBlocking {
            val ledger = kotlin.runCatching { LoadLedgerUseCase(settings, LedgerParsingProcessor()).load(File("data/output.xlsx")) }.getOrThrow()
            val prices = PricesRepository(settings, CryptoCompareClient(defaultHttpClient(), settings, JsonBridge)).getPrices(ledger.assetsTradings).associateBy { it.asset }

            StatsCalculatorUseCase(settings).calculateStats(
                ledger, Filter.AllTransactions, prices, null
            )
        }
    }

    @Test
    fun market() {
        runBlocking {
            val ledger = kotlin.runCatching { LoadLedgerUseCase(settings,LedgerParsingProcessor()).load(File("data/output.xlsx")) }.getOrThrow()
            val history = LoadPriceHistoryUseCase(CryptoCompareClient(defaultHttpClient(), settings, JsonBridge), JsonBridge)
                .loadAll(ledger.assetsForPrices)
                .mapValues { it.value.getOrThrow() }
            StatsCalculatorUseCase(settings).calculateMarketDailyGains(
                ledger, history, "GBP"
            )
        }
    }
}