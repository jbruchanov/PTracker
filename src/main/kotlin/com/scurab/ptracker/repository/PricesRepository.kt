package com.scurab.ptracker.repository

import com.scurab.ptracker.ext.now
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.CoinPrice
import com.scurab.ptracker.model.ExchangeWallet
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.Locations
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.model.CryptoCompareWssResponse
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.serialisation.JsonBridge
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.toJavaLocalDateTime
import java.io.File

class PricesRepository(
    private val client: CryptoCompareClient
) {
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

    fun flowPrices(data: Map<ExchangeWallet, List<Asset>>): Channel<CryptoCompareWssResponse> {
        val args = data.map { (exchange, assets) -> assets.map { asset -> CryptoCompareWssSubscriptionArg(exchange.item, asset) } }.flatten().distinct()
        return client.subscribeTicker(args)
    }
}