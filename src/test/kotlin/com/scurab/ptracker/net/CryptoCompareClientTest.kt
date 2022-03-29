package com.scurab.ptracker.net

import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.serialisation.JsonBridge
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
internal class CryptoCompareClientTest {

    private val testAssets by lazy {
        val crypto = listOf("BTC", "ETH", "ADA", "LTC", "SOL")
        val fiat = listOf("GBP", "USD")
        crypto.map { c -> fiat.map { f -> Asset(c, f) } }.flatten()
    }

    @Test
    fun getCoinData() {
        val client = CryptoCompareClient()
        val coins = listOf("BTC", "ETH")
        runBlocking {
            coins.forEach {
                val coinDetail = client.getCoinData(it)
                println(coinDetail)
            }
        }
    }

    @Test
    fun getHistoryData() {
        val ledger = kotlin.runCatching { LoadLedgerUseCase().load(File("data/output.xlsx")) }.getOrNull()
        val symbols = ledger?.assets?.takeIf { it.isNotEmpty() } ?: testAssets
        runBlocking {
            symbols.forEach { (c, f) ->
                val historyData = CryptoCompareClient().getHistoryData(c, f, 1000)
                File("data/$c-$f.json").writeText(JsonBridge.serialize(historyData.data.items))
            }
        }
    }
}