package com.scurab.ptracker.net

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.model.CryptoCompareCoinDetail
import com.scurab.ptracker.net.model.CryptoCompareHistoryData
import com.scurab.ptracker.net.model.CryptoCompareResult
import com.scurab.ptracker.net.model.CryptoCompareWsResponse
import com.scurab.ptracker.net.model.CryptoCompareWssSubscription
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.ui.model.Validity
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.wss
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CryptoCompareClient(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val settings: AppSettings,
    private val jsonBridge: JsonBridge
) : CoroutineScope {

    private var job = Job()
    override val coroutineContext: CoroutineContext get() = SupervisorJob(job) + Dispatchers.IO

    suspend fun testKey(key: String): Validity {
        TODO()
    }

    suspend fun getHistoryData(cryptoSymbol: String, fiatSymbol: String, limit: Int = 1000, toTs: Long = -1): CryptoCompareResult<CryptoCompareHistoryData> {
        return httpClient.get(historyUrl(cryptoSymbol, fiatSymbol, limit, toTs))
    }

    suspend fun getCoinData(cryptoSymbol: String): CryptoCompareResult<Map<String, CryptoCompareCoinDetail>> {
        return httpClient.get(coinUrl(cryptoSymbol))
    }

    suspend fun getPrices(assets: Collection<Asset>, primaryFiatCoin: FiatCoin? = null): List<CoinPrice> {
        if (assets.isEmpty()) return emptyList()
        val fromSyms = assets.map { it.coin2 }.distinct().joinToString(separator = ",")
        val toCoins = (assets.map { it.coin1 } + primaryFiatCoin?.item).filterNotNull().distinct()
        val toSyms = toCoins.joinToString(separator = ",")
        val rawData = httpClient.get<Map<String, Map<String, Double>>>(pricesUrl(toSyms, fromSyms))
        val result = rawData.mapNotNull { (c1, v) -> v.mapNotNull { (c2, price) -> CoinPrice.fromUnknownPairOrNull(c1, c2, price.toBigDecimal()) } }
            .flatten()
        return result
    }

    fun subscribeTicker(args: List<CryptoCompareWssSubscriptionArg>): Channel<CryptoCompareWsResponse> {
        val apiKey = settings.cryptoCompareApiKey
        requireNotNull(apiKey) { "cryptoCompareApiKey is null" }
        return subscribeTicker(args, apiKey)
    }

    private fun subscribeTicker(args: List<CryptoCompareWssSubscriptionArg>, apiKey: String): Channel<CryptoCompareWsResponse> {
        var wss: Job? = null
        val channel = Channel<CryptoCompareWsResponse>().apply {
            invokeOnClose {
                requireNotNull(wss).cancel()
            }
        }

        wss = launch {
            httpClient.wss(
                method = HttpMethod.Get, host = wsUrl, path = webSocketPath(apiKey)
            ) {
                val msg = CryptoCompareWssSubscription(args)
                send(Frame.Text(jsonBridge.serialize(msg)))
                while (isActive) {
                    when (val frame = incoming.receive()) {
                        is Frame.Text -> {
                            val message = frame.data.decodeToString()
                            val parse = runCatching { jsonBridge.deserialize<CryptoCompareWsResponse>(message) }
                            val obj = parse.getOrNull()
                            when (obj) {
                                is CryptoCompareWsResponse.Error -> throw IllegalStateException(message)
                                null -> {
                                    System.err.println("Parsing error?\n" +
                                            "json:${message}\n" +
                                            "${parse.exceptionOrNull()?.message}\n" +
                                            "${parse.exceptionOrNull()?.stackTraceToString()}")
                                }
                                else -> channel.trySend(obj)
                            }
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

    fun stop() {
        job.cancel()
        job = Job()
    }

    companion object {
        private const val mainUrl = "https://min-api.cryptocompare.com"
        private const val wsUrl = "streamer.cryptocompare.com"
        private fun historyUrl(fsym: String, tsym: String, limit: Int, toTs: Long) = "${mainUrl}/data/v2/histoday?fsym=$fsym&tsym=$tsym&limit=$limit&toTs=$toTs"
        private fun coinUrl(fsym: String, apiKey: String? = null) = "${mainUrl}/data/all/coinlist?fsym=$fsym" + (apiKey?.let { "&api_key=${apiKey}" } ?: "")
        private fun pricesUrl(cryptoSyms: String, fiatSyms: String) = "${mainUrl}/data/pricemulti?fsyms=${cryptoSyms}&tsyms=${fiatSyms}"
        private fun webSocketPath(key: String) = "/v2?api_key=${key}"
    }
}