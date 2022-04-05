package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.existsAndHasSize
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.model.CryptoComparePriceItem
import java.io.File

class LoadPriceHistoryUseCase(
    private val cryptoCompareClient: CryptoCompareClient,
    private val jsonBridge: JsonBridge
) {

    private val location = File(Locations.Daily)

    suspend fun loadAll(assets: List<Asset>): Map<Asset, Result<List<PriceItem>>> = assets.associateWith { kotlin.runCatching { load(it) } }

    suspend fun load(asset: Asset): List<PriceItem> {
        location.mkdirs()
        val f = File(location, "${asset.label}.json")

        var result: List<PriceItem>? = null
        if (f.existsAndHasSize()) {
            result =
                jsonBridge.deserialize<List<CryptoComparePriceItem>>(f.readText()).mapIndexed { index, cryptoComparePriceItem -> PriceItem(index, asset, cryptoComparePriceItem) }
        }
        if (result == null) {
            result = cryptoCompareClient.getHistoryData(asset.coin1, asset.coin2).data.items
                .also { f.writeText(jsonBridge.serialize(it)) }
                .mapIndexed { index, cryptoComparePriceItem -> PriceItem(index, asset, cryptoComparePriceItem) }
        }

        return result
    }
}