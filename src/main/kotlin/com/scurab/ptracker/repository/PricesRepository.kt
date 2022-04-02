package com.scurab.ptracker.repository

import com.scurab.ptracker.component.ProcessScope
import com.scurab.ptracker.ext.now
import com.scurab.ptracker.ext.sign
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.CoinPrice
import com.scurab.ptracker.model.ExchangeWallet
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.Locations
import com.scurab.ptracker.model.MarketPrice
import com.scurab.ptracker.model.WsExchangeResponse
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.serialisation.JsonBridge
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import java.io.File
import kotlin.random.Random

class PricesRepository(
    private val client: CryptoCompareClient
) {
    data class MessageToken(
        val timestamp: Long,
        val client: String
    )

    data class Subscription(
        val exchangeWallet: ExchangeWallet,
        val asset: Asset
    )

    private val _wsMarketPrice = MutableSharedFlow<MarketPrice>(16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wsMarketPrice = _wsMarketPrice.asSharedFlow()

    private val _wsTickToken = MutableSharedFlow<MessageToken>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wsTickToken = _wsTickToken.asSharedFlow()

    private val _subscriptions = mutableSetOf<Subscription>()
    val subscriptions: Collection<Subscription> = _subscriptions

    suspend fun getPrices(ledger: Ledger) = client.getPrices(ledger.assets)

    suspend fun getPrices(assets: List<Asset>): List<CoinPrice> {
        val file = File(Locations.Data, "prices-${now().toJavaLocalDateTime().format(DateTimeFormats.debugFullDate)}.json")
        return if (file.exists()) {
            JsonBridge.deserialize(file.readText())
        } else {
            client.getPrices(assets).also {
                file.writeText(JsonBridge.serialize(it, beautify = true))
            }
        }
    }

    private fun flowPrices(data: Map<ExchangeWallet, List<Asset>>): Channel<out WsExchangeResponse> {
        val args = data.map { (exchange, assets) -> assets.map { asset -> CryptoCompareWssSubscriptionArg(exchange.item, asset) } }.flatten().distinct()
        return client.subscribeTicker(args)
    }

    private var _wsSubscriptionJob: Job? = null

    //TODO: handle missing CC API Key
    fun subscribeWs(data: Map<ExchangeWallet, List<Asset>>) {
        _wsSubscriptionJob?.cancel()
        _wsSubscriptionJob = ProcessScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    flowPrices(data).consumeEach {
                        when (it) {
                            is WsExchangeResponse.MarketPrice -> _wsMarketPrice.tryEmit(it)
                            is WsExchangeResponse.Subscription -> _subscriptions.add(Subscription(it.exchangeWallet, it.asset))
                        }
                        _wsTickToken.tryEmit(MessageToken(System.currentTimeMillis(), it.client))
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun subscribeWsRandomPrices(data: Map<ExchangeWallet, List<Asset>>) {
        _wsSubscriptionJob?.cancel()
        _wsSubscriptionJob = ProcessScope.launch(Dispatchers.IO) {
            val prices = getPrices(data.map { it.value }.flatten().distinct()).toMutableList()
            while (isActive) {
                prices.forEachIndexed { index, coinPrice ->
                    if (Random.nextBoolean()) return@forEachIndexed
                    val offset = 1 + (Random.nextInt(1, 5) / 100f * Random.nextBoolean().sign())
                    val marketPrice = coinPrice.copy(price = coinPrice.price * offset.toBigDecimal())
                    _wsMarketPrice.tryEmit(marketPrice)
                    _wsTickToken.tryEmit(MessageToken(System.currentTimeMillis(), "Demo"))
                    prices[index] = marketPrice
                    delay(Random.nextLong(1000, 2000))
                }
            }
        }
    }
}