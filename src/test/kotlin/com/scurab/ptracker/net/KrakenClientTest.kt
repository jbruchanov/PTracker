package com.scurab.ptracker.net

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.WsExchangeResponse
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

//integration tests against live kraken API
@Disabled
internal class KrakenClientTest {

    private val client = KrakenClient(defaultHttpClient(), JsonBridge)

    @Test
    fun getPrices() {
        val assets = listOf(
            Asset("BTC", "USD"),
            Asset("ADA", "EUR"),
            Asset("DOGE", "GBP"),
            Asset("LUNC", "USD"),
            //fiat crosses
            Asset("EUR", "GBP"),
            Asset("USD", "GBP"),
            //identity
            Asset("GBP", "GBP"),
            //not on kraken -> skipped
            Asset("ETHW", "USD")
        )
        val prices = runBlocking { client.getPrices(assets) }
        prices.forEach { println(it) }
        val byAsset = prices.associateBy { it.asset }
        assertEquals(assets.size - 1, prices.size)
        assertTrue(byAsset.getValue(Asset("BTC", "USD")).price.toDouble() > 1000.0)
        assertTrue(byAsset.getValue(Asset("EUR", "GBP")).price.toDouble() in 0.5..1.5)
        assertTrue(byAsset.getValue(Asset("USD", "GBP")).price.toDouble() in 0.5..1.5)
        assertEquals(0, byAsset.getValue(Asset("GBP", "GBP")).price.compareTo(java.math.BigDecimal.ONE))
    }

    @Test
    fun getDailyHistory() {
        val items = runBlocking { client.getDailyHistory("BTC", "USD", limit = 10) }
        items.forEach { println("${it.localDate} o=${it.open} h=${it.high} l=${it.low} c=${it.close}") }
        assertEquals(11, items.size)
        //no gaps
        assertTrue(items.zipWithNext().all { (a, b) -> b.time - a.time == 86400L })
    }

    @Test
    fun getDailyHistoryOverCap() {
        val items = runBlocking { client.getDailyHistory("BTC", "USD", limit = 1000) }
        println("items:${items.size}, first:${items.first().localDate}, last:${items.last().localDate}")
        assertTrue(items.size in 700..721)
    }

    @Test
    fun wss() {
        val assets = listOf(Asset("BTC", "GBP"), Asset("ETH", "USD"), Asset("EUR", "GBP"))
        val channel = client.subscribeTicker(assets.map { CryptoCompareWssSubscriptionArg("Kraken", it) })
        val received = mutableListOf<WsExchangeResponse>()
        val job = GlobalScope.launch(Dispatchers.IO) {
            channel.consumeEach {
                println("Receive:$it")
                received.add(it)
            }
        }
        Thread.sleep(10000)
        job.cancel()
        channel.cancel()
        assertTrue(received.filterIsInstance<WsExchangeResponse.Subscription>().size == assets.size)
        val tickers = received.filterIsInstance<WsExchangeResponse.MarketPrice>()
        assertTrue(tickers.isNotEmpty())
        assertTrue(tickers.map { it.asset }.toSet().containsAll(assets))
    }
}
