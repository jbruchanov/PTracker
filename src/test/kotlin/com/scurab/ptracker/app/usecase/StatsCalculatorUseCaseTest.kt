package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.convertTradePrice
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.repository.MemoryAppSettings
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.app.util.LedgerParsingProcessor
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
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
            val ledger = kotlin.runCatching { LoadLedgerUseCase(settings, LedgerParsingProcessor()).load(File("data/BittyTax_Records.xlsx")) }.getOrThrow()
            val prices = PricesRepository(settings, CryptoCompareClient(defaultHttpClient(), settings, JsonBridge)).getPrices(ledger.assetsTradings).associateBy { it.asset }

            StatsCalculatorUseCase(settings).calculateStats(
                ledger, Filter.AllTransactions, prices, "GBP"
            )
        }
    }

    @Test
    fun testConvertToTradePrice() {
        val ledger = kotlin.runCatching { LoadLedgerUseCase(settings, LedgerParsingProcessor()).load(File("data/BittyTax_Records.xlsx")) }.getOrThrow()
        runBlocking {
            val prices = PricesRepository(settings, CryptoCompareClient(defaultHttpClient(), settings, JsonBridge)).getPrices(ledger.assetsTradings).associateBy { it.asset }
            ledger.items.first { it.asset == Asset("BTC", "ETH") }
                .convertTradePrice(prices, "GBP")
        }
    }

    @Test
    fun market() {
        runBlocking {
            val ledger = kotlin.runCatching { LoadLedgerUseCase(settings, LedgerParsingProcessor()).load(File("data/output.xlsx")) }.getOrThrow()
            val history = LoadPriceHistoryUseCase(CryptoCompareClient(defaultHttpClient(), settings, JsonBridge), JsonBridge)
                .loadAll(ledger.assetsForPrices)
                .mapValues { it.value.getOrThrow() }
            val asset = Asset("BTC", "GBP")
            StatsCalculatorUseCase(settings).calculateMarketDailyGains(
                ledger.items.filter { it.hasAsset(asset) }.take(10),
                history,
                "GBP",
                dateGrouping = DateGrouping.Day,
                doSumCrypto = true
            )
        }
    }
}