package com.scurab.ptracker.net

import com.scurab.ptracker.net.model.CryptoCompareCoinDetail
import com.scurab.ptracker.net.model.CryptoCompareHistoryData
import com.scurab.ptracker.net.model.CryptoCompareResult
import com.scurab.ptracker.net.model.CryptoCompareWssResponse
import com.scurab.ptracker.net.model.CryptoCompareWssSubscription
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.repository.AppSettings
import com.scurab.ptracker.serialisation.JsonBridge
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.wss
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    override var coroutineContext: CoroutineContext = job + Dispatchers.IO

    suspend fun getHistoryData(cryptoSymbol: String, fiatSymbol: String, limit: Int = 1000, toTs: Long = -1): CryptoCompareResult<CryptoCompareHistoryData> {
        return httpClient.get(historyUrl(cryptoSymbol, fiatSymbol, limit, toTs))
    }

    suspend fun getCoinData(cryptoSymbol: String): CryptoCompareResult<Map<String, CryptoCompareCoinDetail>> {
        return httpClient.get(coinUrl(cryptoSymbol))
    }

    fun subscribeTicker(args: List<CryptoCompareWssSubscriptionArg>): Channel<CryptoCompareWssResponse.MarketTicker> {
        val apiKey = settings.cryptoCompareApiKey
        requireNotNull(apiKey) { "cryptoCompareApiKey is null" }
        var wss: Job? = null

        val channel = Channel<CryptoCompareWssResponse.MarketTicker>().apply {
            invokeOnClose {
                wss?.cancel()
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
                            val obj = jsonBridge.deserialize<CryptoCompareWssResponse>(message)
                            if (obj is CryptoCompareWssResponse.MarketTicker) {
                                channel.trySend(obj)
                            }

                        }
                    }
                }
                wss?.cancel()
            }
        }
        return channel
    }

    fun stop() {
        job.cancel()
        job = Job()
        coroutineContext = job + Dispatchers.Default
    }

    companion object {
        private const val mainUrl = "https://min-api.cryptocompare.com"
        private const val wsUrl = "streamer.cryptocompare.com"
        private fun historyUrl(fsym: String, tsym: String, limit: Int, toTs: Long) = "${mainUrl}/data/v2/histoday?fsym=$fsym&tsym=$tsym&limit=$limit&toTs=$toTs"
        private fun coinUrl(fsym: String) = "${wsUrl}/data/all/coinlist?fsym=$fsym"
        private fun webSocketPath(key: String) = "/v2?api_key=${key}"
    }
}