package com.scurab.ptracker.net

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.app.usecase.LoadLedgerUseCase
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
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
        val client = CryptoCompareClient(defaultHttpClient(), mockk(), JsonBridge)
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
        val folder = File(Locations.Daily)
        folder.mkdirs()
        runBlocking {
            symbols.forEach { (c, f) ->
                val historyData = CryptoCompareClient(defaultHttpClient(), mockk(), JsonBridge).getHistoryData(c, f, 1000)
                File(folder, "$c-$f.json").writeText(JsonBridge.serialize(historyData.data.items))
            }
        }
    }

    @Test
    fun wss() {
        val client = CryptoCompareClient(defaultHttpClient(), mockk(), JsonBridge)
        val assets = listOf(Asset("BTC", "GBP"), Asset("ETH", "USD"))
        val channel = client.subscribeTicker(assets.map { CryptoCompareWssSubscriptionArg("Coinbase", it) })
        val job = GlobalScope.launch(Dispatchers.IO) {
            channel.consumeEach {
                println("Receive:$it")
            }
        }
        Thread.sleep(30000)
        job.cancel()
    }
}