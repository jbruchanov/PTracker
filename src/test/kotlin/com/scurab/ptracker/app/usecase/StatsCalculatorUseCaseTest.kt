package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.repository.AppSettingsJsonRepository
import com.scurab.ptracker.app.repository.MemoryAppSettings
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

//@Disabled
internal class StatsCalculatorUseCaseTest {

    @Test
    fun man() {
        runBlocking {
            val settings = MemoryAppSettings().apply { primaryCoin = "GBP" }
            val ledger = kotlin.runCatching { LoadLedgerUseCase(settings).load(File("data/output2.xlsx")) }.getOrThrow()
            val prices = PricesRepository(settings, CryptoCompareClient(defaultHttpClient(), settings, JsonBridge)).getPrices(ledger.assetsTradings)

            StatsCalculatorUseCase(MemoryAppSettings()).calculateStats(
                ledger, Filter.AllTransactions, prices, "GBP"
            )
        }
    }
}