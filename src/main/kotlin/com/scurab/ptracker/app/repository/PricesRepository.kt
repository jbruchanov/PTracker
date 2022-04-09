package com.scurab.ptracker.app.repository

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.fiatCoins
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.sign
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.CoinPrice.Companion.asCoinPrice
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.WsExchangeResponse
import com.scurab.ptracker.app.model.WsMessageToken
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.component.ProcessScope
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
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
    private val appSettings: AppSettings,
    private val client: CryptoCompareClient
) {
    data class Subscription(
        val exchangeWallet: ExchangeWallet,
        val asset: Asset
    )

    private val _wsMarketPrice = MutableSharedFlow<MarketPrice>(16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wsMarketPrice = _wsMarketPrice.asSharedFlow()

    private val _wsTickToken = MutableSharedFlow<WsMessageToken>(1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val wsTickToken = _wsTickToken.asSharedFlow()

    private val _subscriptions = mutableSetOf<Subscription>()
    val subscriptions: Collection<Subscription> = _subscriptions

    private val _latestPrices = mutableMapOf<Asset, MarketPrice>()
    val latestPrices: Map<Asset, MarketPrice> = _latestPrices

    suspend fun getPrices(ledger: Ledger) = client.getPrices(ledger.assetsForPrices)

    private val folder = File(Locations.Prices)
    suspend fun getPrices(assets: List<Asset>): List<CoinPrice> {
        val primaryCoin = appSettings.primaryCoin?.let { FiatCoin(it) }
        val primaryCoinAssets = primaryCoin?.let { primaryCurrency -> assets.fiatCoins().map { coin -> Asset(coin, primaryCurrency.item) } } ?: emptyList()
        val request = (assets + primaryCoinAssets).distinct()
        if (_latestPrices.keys.containsAll(request)) {
            return _latestPrices.values.map { it.asCoinPrice() }
        }
        folder.mkdirs()
        val file = File(folder, "prices-${now().toJavaLocalDateTime().format(DateTimeFormats.debugFullDate)}.json")
        val localData = (if (file.exists()) JsonBridge.deserialize<List<CoinPrice>>(file.readText()).toSet() else emptySet()).associateBy { it.asset }
        val onlineData = (if (!localData.keys.containsAll(request)) {
            val missingAssets = request - localData.keys
            val toRequest = missingAssets.filter { !it.isIdentity }
            if (toRequest.isNotEmpty()) client.getPrices(request, primaryCoin).toSet() else missingAssets.map { CoinPrice(it, 1.bd) }
        } else emptySet()).associateBy { it.asset }
        val result = (localData.keys + onlineData.keys).mapNotNull { onlineData[it] ?: localData[it] }
        if (onlineData.isNotEmpty()) {
            file.writeText(JsonBridge.serialize(result, beautify = true))
        }
        _latestPrices.putAll(result.associateBy(keySelector = { it.asset }, valueTransform = { it }))
        return result.toList()
    }

    private fun flowPrices(data: Map<ExchangeWallet, List<Asset>>): Channel<out WsExchangeResponse> {
        val args = data.map { (exchange, assets) -> assets.map { asset -> CryptoCompareWssSubscriptionArg(exchange.item, asset) } }.flatten().distinct()
        return client.subscribeTicker(args)
    }

    private var _wsSubscriptionJob: Job? = null

    //TODO: handle missing CC API Key
    fun subscribeWs(data: Map<ExchangeWallet, List<Asset>>) {
        //TODO: values for primary coin
        _wsSubscriptionJob?.cancel()
        _wsSubscriptionJob = ProcessScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    flowPrices(data).consumeEach {
                        when (it) {
                            is WsExchangeResponse.MarketPrice -> {
                                println("RCV MarketPrice:$it")
                                _wsMarketPrice.tryEmit(it)
                                _latestPrices[it.asset] = it
                            }
                            is WsExchangeResponse.Subscription -> _subscriptions.add(Subscription(it.exchangeWallet, it.asset))
                        }
                        _wsTickToken.tryEmit(WsMessageToken(System.currentTimeMillis(), it.client))
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
                repeat((prices.size / 2)) {
                    prices.forEachIndexed { index, coinPrice ->
                        if (Random.nextBoolean()) return@forEachIndexed
                        val offset = 1 + (Random.nextInt(1, 3) / 100f * Random.nextBoolean().sign())
                        val marketPrice = coinPrice.copy(price = coinPrice.price * offset.toBigDecimal())
                        _wsMarketPrice.tryEmit(marketPrice)
                        _wsTickToken.tryEmit(WsMessageToken(System.currentTimeMillis(), "Demo"))
                        _latestPrices[marketPrice.asset] = marketPrice
                        prices[index] = marketPrice
                    }
                    delay(Random.nextLong(2000, 5000))
                }
            }
        }
    }
}