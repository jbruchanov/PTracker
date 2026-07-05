package com.scurab.ptracker.net

import com.scurab.ptracker.app.ext.inverse
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.model.CryptoComparePriceItem
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.net.model.KrakenWsResponse
import com.scurab.ptracker.ui.model.Validity
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.math.BigDecimal
import kotlin.coroutines.CoroutineContext

/**
 * Free, keyless market data client based on the public Kraken API,
 * replacement for [CryptoCompareClient] whose free tier was retired in 05/2026.
 */
class KrakenClient(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val jsonBridge: JsonBridge
) : MarketDataClient, CoroutineScope {

    private var job = Job()
    override val coroutineContext: CoroutineContext get() = SupervisorJob(job) + Dispatchers.IO

    private val pairsMutex = Mutex()
    private var pairsCache: Map<Pair<String, String>, KrakenPair>? = null

    //no key needed, just check the API is reachable
    override suspend fun testKey(key: String): Validity =
        kotlin.runCatching { apiResult(timeUrl) }.map { Validity.Tested }.getOrDefault(Validity.Unknown)

    override suspend fun getDailyHistory(cryptoSymbol: String, fiatSymbol: String, limit: Int): List<CryptoComparePriceItem> {
        val pairs = assetPairs()
        val pair = pairs[cryptoSymbol to fiatSymbol]
        val flippedPair = if (pair == null) pairs[fiatSymbol to cryptoSymbol] else null
        val krakenPair = pair ?: flippedPair ?: throw IllegalStateException("No kraken pair for $cryptoSymbol-$fiatSymbol")
        val cappedLimit = limit.coerceAtMost(MaxOhlcCandles)
        if (cappedLimit < limit) {
            println("KrakenClient: OHLC data are limited to last $MaxOhlcCandles daily candles, requested:$limit, asset:$cryptoSymbol-$fiatSymbol")
        }
        val since = todayUtcMidnightSeconds() - (cappedLimit * DaySeconds) - 1
        val result = apiResult(ohlcUrl(krakenPair.pairId, since))
        val candles = (result[krakenPair.pairId] ?: result.entries.firstOrNull { it.key != "last" }?.value)?.jsonArray ?: return emptyList()
        return candles.map { candle ->
            val v = candle.jsonArray
            val open = v[1].jsonPrimitive.content.toBigDecimal()
            val high = v[2].jsonPrimitive.content.toBigDecimal()
            val low = v[3].jsonPrimitive.content.toBigDecimal()
            val close = v[4].jsonPrimitive.content.toBigDecimal()
            val vwap = v[5].jsonPrimitive.content.toBigDecimal()
            val volume = v[6].jsonPrimitive.content.toBigDecimal()
            val flip = flippedPair != null
            CryptoComparePriceItem(
                time = v[0].jsonPrimitive.long,
                high = if (flip) low.inverse() else high,
                low = if (flip) high.inverse() else low,
                open = if (flip) open.inverse() else open,
                close = if (flip) close.inverse() else close,
                volumeFrom = volume,
                volumeTo = volume * vwap,
                conversionType = null,
                conversionSymbol = null
            )
        }
    }

    override suspend fun getCoinIconUrl(coin: String): String? = "$IconsUrl${coin.lowercase()}.png"

    override suspend fun getPrices(assets: Collection<Asset>, primaryFiatCoin: FiatCoin?): List<CoinPrice> {
        if (assets.isEmpty()) return emptyList()
        val requested = assets.distinct()
        val identities = requested.filter { it.isIdentity }.map { CoinPrice(it, BigDecimal.ONE) }
        val pairs = assetPairs()
        val lookups = requested
            .filterNot { it.isIdentity }
            .mapNotNull { asset -> (pairs[asset.coin1 to asset.coin2] ?: pairs[asset.coin2 to asset.coin1])?.let { asset to it } }
        val skipped = requested.filterNot { it.isIdentity } - lookups.map { it.first }.toSet()
        if (skipped.isNotEmpty()) {
            println("KrakenClient: no pairs found for assets:$skipped")
        }
        if (lookups.isEmpty()) return identities
        val result = apiResult(tickerUrl(lookups.map { (_, pair) -> pair.pairId }.distinct()))
        val lastPrices = result.entries.mapNotNull { (pairId, v) ->
            v.jsonObject["c"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull()?.let { pairId to it }
        }.toMap()
        val coinPrices = lookups.mapNotNull { (asset, pair) ->
            val last = lastPrices[pair.pairId] ?: return@mapNotNull null
            //oriented so the price is always 1*asset.coin1 = price*asset.coin2
            val price = if (pair.appCoin1 == asset.coin1) last else last.inverse()
            CoinPrice(asset, price)
        }
        return identities + coinPrices
    }

    override fun subscribeTicker(args: List<CryptoCompareWssSubscriptionArg>): Channel<KrakenWsResponse> {
        var wss: Job? = null
        //buffered so bursts (subscribe acks, snapshots) aren't dropped by trySend when the consumer is busy
        val channel = Channel<KrakenWsResponse>(capacity = Channel.UNLIMITED).apply {
            invokeOnClose {
                requireNotNull(wss).cancel()
            }
        }

        wss = launch {
            val argsBySymbol = args.associateBy { it.asset.wsSymbol() }
            val subscribe = buildJsonObject {
                put("method", "subscribe")
                putJsonObject("params") {
                    put("channel", "ticker")
                    putJsonArray("symbol") { argsBySymbol.keys.forEach { add(it) } }
                }
            }
            httpClient.wss(
                method = HttpMethod.Get, host = wsUrl, path = wsPath
            ) {
                send(Frame.Text(subscribe.toString()))
                while (isActive) {
                    when (val frame = incoming.receive()) {
                        is Frame.Text -> {
                            val message = frame.data.decodeToString()
                            kotlin.runCatching { parseWsMessage(message, argsBySymbol) }
                                .onFailure {
                                    System.err.println(
                                        "Parsing error?\n" +
                                            "json:${message}\n" +
                                            "${it.message}\n" +
                                            it.stackTraceToString()
                                    )
                                }
                                .getOrNull()
                                ?.forEach { channel.trySend(it) }
                        }
                        else -> Unit
                    }
                }
            }
        }.also { job ->
            job.invokeOnCompletion { ex ->
                channel.cancel(cause = ex?.let { CancellationException("Cancel, ${it.message}") })
            }
        }
        return channel
    }

    override fun stop() {
        job.cancel()
        job = Job()
    }

    private fun parseWsMessage(message: String, argsBySymbol: Map<String, CryptoCompareWssSubscriptionArg>): List<KrakenWsResponse> {
        val obj = jsonBridge.json.parseToJsonElement(message).jsonObject
        val channelName = obj["channel"]?.jsonPrimitive?.contentOrNull
        val method = obj["method"]?.jsonPrimitive?.contentOrNull
        return when {
            channelName == "ticker" -> obj["data"]?.jsonArray.orEmpty().mapNotNull { item ->
                val ticker = item.jsonObject
                val symbol = ticker["symbol"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val asset = argsBySymbol[symbol]?.asset ?: return@mapNotNull null
                val price = ticker["last"]?.jsonPrimitive?.contentOrNull?.toBigDecimalOrNull() ?: return@mapNotNull null
                KrakenWsResponse.MarketTicker(asset, price)
            }
            channelName == "heartbeat" -> listOf(KrakenWsResponse.HeartBeat)
            method == "subscribe" -> {
                val success = obj["success"]?.jsonPrimitive?.booleanOrNull ?: false
                val symbol = obj["result"]?.jsonObject?.get("symbol")?.jsonPrimitive?.contentOrNull
                val arg = symbol?.let { argsBySymbol[it] }
                when {
                    success && arg != null -> listOf(KrakenWsResponse.SubscriptionDone(ExchangeWallet(arg.exchange), arg.asset))
                    success -> emptyList()
                    else -> listOf(KrakenWsResponse.SubscriptionError(obj["error"]?.jsonPrimitive?.contentOrNull ?: message))
                }
            }
            else -> emptyList()
        }
    }

    private suspend fun assetPairs(): Map<Pair<String, String>, KrakenPair> = pairsMutex.withLock {
        pairsCache ?: apiResult(assetPairsUrl)
            .entries
            .mapNotNull { (pairId, v) ->
                val wsName = v.jsonObject["wsname"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                val coins = wsName.split("/").takeIf { it.size == 2 } ?: return@mapNotNull null
                KrakenPair(pairId, wsName, coins[0].toAppCoin(), coins[1].toAppCoin())
            }
            .associateBy { it.appCoin1 to it.appCoin2 }
            .also { pairsCache = it }
    }

    private suspend fun apiResult(url: String): JsonObject {
        val response = httpClient.get(url)
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Kraken request failed, status:${response.status.value}, url:$url")
        }
        val obj = jsonBridge.json.parseToJsonElement(response.bodyAsText()).jsonObject
        val errors = obj["error"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
        if (errors.isNotEmpty()) {
            throw IllegalStateException("Kraken request failed, error:$errors, url:$url")
        }
        return obj["result"]?.jsonObject ?: throw IllegalStateException("Kraken response has no result, url:$url")
    }

    private data class KrakenPair(
        val pairId: String,
        val wsName: String,
        val appCoin1: String,
        val appCoin2: String
    )

    companion object {
        private const val mainUrl = "https://api.kraken.com/0/public"
        private const val wsUrl = "ws.kraken.com"
        private const val wsPath = "/v2"
        private const val timeUrl = "$mainUrl/Time"
        private const val assetPairsUrl = "$mainUrl/AssetPairs"
        private const val DaySeconds = 86400L
        //kraken OHLC endpoint returns at most 720 most recent candles
        private const val MaxOhlcCandles = 720
        private const val IconsUrl = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/"

        //app symbol -> kraken ws v2 symbol (v2 uses common codes like BTC/DOGE, unlike REST wsname XBT/XDG)
        private val appToKrakenWs = mapOf("LUNC" to "LUNA")

        //kraken (REST wsname or ws v2) symbol -> app symbol
        private val krakenToApp = mapOf("XBT" to "BTC", "XDG" to "DOGE", "LUNA" to "LUNC")

        private fun ohlcUrl(pairId: String, sinceSeconds: Long) = "$mainUrl/OHLC?pair=$pairId&interval=1440&since=$sinceSeconds"
        private fun tickerUrl(pairIds: Collection<String>) = "$mainUrl/Ticker?pair=${pairIds.joinToString(",")}"
        private fun todayUtcMidnightSeconds(): Long = (System.currentTimeMillis() / 1000L).let { it - (it % DaySeconds) }
        private fun String.toAppCoin() = krakenToApp[this] ?: this
        private fun String.toWsCoin() = appToKrakenWs[this] ?: this
        private fun Asset.wsSymbol() = "${coin1.toWsCoin()}/${coin2.toWsCoin()}"
    }
}
