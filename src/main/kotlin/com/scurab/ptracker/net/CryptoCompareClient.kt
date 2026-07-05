package com.scurab.ptracker.net

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.model.CryptoCompareCoinDetail
import com.scurab.ptracker.net.model.CryptoCompareErrorEnvelope
import com.scurab.ptracker.net.model.CryptoCompareHistoryData
import com.scurab.ptracker.net.model.CryptoComparePriceItem
import com.scurab.ptracker.net.model.CryptoCompareResult
import com.scurab.ptracker.net.model.CryptoCompareWsResponse
import com.scurab.ptracker.net.model.CryptoCompareWssSubscription
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.ui.model.Validity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.wss
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
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
import kotlin.coroutines.CoroutineContext

class CryptoCompareClient(
    private val httpClient: HttpClient = defaultHttpClient(),
    private val settings: AppSettings,
    private val jsonBridge: JsonBridge
) : MarketDataClient, CoroutineScope {

    private var job = Job()
    override val coroutineContext: CoroutineContext get() = SupervisorJob(job) + Dispatchers.IO

    override suspend fun testKey(key: String): Validity {
        val response = apiGet(pricesUrl("BTC", "USD"), apiKey = key)
        return when {
            response.status.isSuccess() -> Validity.Tested
            response.status == HttpStatusCode.Unauthorized || response.status == HttpStatusCode.Forbidden -> Validity.Invalid
            else -> Validity.Unknown
        }
    }

    override suspend fun getDailyHistory(cryptoSymbol: String, fiatSymbol: String, limit: Int): List<CryptoComparePriceItem> {
        return getHistoryData(cryptoSymbol, fiatSymbol, limit).data.items
    }

    override suspend fun getCoinIconUrl(coin: String): String? {
        return getCoinData(coin).data[coin]?.fullImageUrl
    }

    suspend fun getHistoryData(cryptoSymbol: String, fiatSymbol: String, limit: Int = 1000, toTs: Long = -1): CryptoCompareResult<CryptoCompareHistoryData> {
        return apiGet(historyUrl(cryptoSymbol, fiatSymbol, limit, toTs)).validated().body()
    }

    suspend fun getCoinData(cryptoSymbol: String): CryptoCompareResult<Map<String, CryptoCompareCoinDetail>> {
        return apiGet(coinUrl(cryptoSymbol)).validated().body()
    }

    override suspend fun getPrices(assets: Collection<Asset>, primaryFiatCoin: FiatCoin?): List<CoinPrice> {
        if (assets.isEmpty()) return emptyList()
        val fromSyms = assets.map { it.coin2 }.distinct().joinToString(separator = ",")
        val toCoins = (assets.map { it.coin1 } + primaryFiatCoin?.item).filterNotNull().distinct()
        val toSyms = toCoins.joinToString(separator = ",")
        val rawData = apiGet(pricesUrl(toSyms, fromSyms)).validated().body<Map<String, Map<String, Double>>>()
        val result = rawData.mapNotNull { (c1, v) -> v.mapNotNull { (c2, price) -> CoinPrice.fromUnknownPairOrNull(c1, c2, price.toBigDecimal()) } }
            .flatten()
        return result
    }

    private suspend fun apiGet(url: String, apiKey: String? = settings.cryptoCompareApiKey): HttpResponse =
        httpClient.get(url) {
            apiKey?.takeIf { it.isNotBlank() }?.let { header(HttpHeaders.Authorization, "Apikey $it") }
        }

    private suspend fun HttpResponse.validated(): HttpResponse {
        if (!status.isSuccess()) {
            val message = runCatching { body<CryptoCompareErrorEnvelope>().err?.message }.getOrNull()
            throw IllegalStateException("CryptoCompare request failed, status:${status.value}, message:${message ?: "n/a"}, url:${call.request.url}")
        }
        return this
    }

    override fun subscribeTicker(args: List<CryptoCompareWssSubscriptionArg>): Channel<CryptoCompareWsResponse> {
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
                                    System.err.println(
                                        "Parsing error?\n" +
                                            "json:${message}\n" +
                                            "${parse.exceptionOrNull()?.message}\n" +
                                            "${parse.exceptionOrNull()?.stackTraceToString()}"
                                    )
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

    override fun stop() {
        job.cancel()
        job = Job()
    }

    companion object {
        private const val mainUrl = "https://min-api.cryptocompare.com"
        private const val wsUrl = "streamer.cryptocompare.com"
        private fun historyUrl(fsym: String, tsym: String, limit: Int, toTs: Long) = "$mainUrl/data/v2/histoday?fsym=$fsym&tsym=$tsym&limit=$limit&toTs=$toTs"
        private fun coinUrl(fsym: String) = "$mainUrl/data/all/coinlist?fsym=$fsym"
        private fun pricesUrl(cryptoSyms: String, fiatSyms: String) = "$mainUrl/data/pricemulti?fsyms=$cryptoSyms&tsyms=$fiatSyms"
        private fun webSocketPath(key: String) = "/v2?api_key=$key"
    }
}
