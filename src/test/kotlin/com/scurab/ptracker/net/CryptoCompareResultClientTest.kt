package com.scurab.ptracker.net

import com.scurab.ptracker.serialisation.JsonBridge
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File

internal class CryptoCompareResultClientTest {

    @Test
    fun getHistoryData() {
        val crypto = listOf("BTC", "ETH", "ADA", "LTC", "SOL")
        val fiat = listOf("GBP", "USD")

        val symbols = crypto.map { c ->
            fiat.map { f -> Pair(c, f) }
        }.flatten()
        runBlocking {
            symbols.forEach { (c, f) ->
                val historyData = CryptoCompareClient().getHistoryData(c, f, 1000)
                File("data/$c-$f.json").writeText(JsonBridge.serialize(historyData.data.items))
            }
        }
    }
}