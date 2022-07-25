package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.existsAndHasSize
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.model.CryptoComparePriceItem
import kotlinx.coroutines.delay
import kotlinx.datetime.daysUntil
import java.io.File

private class LoadMark(
    var timeStamp: Long,
    var counter: Long
)

class LoadPriceHistoryUseCase(
    private val cryptoCompareClient: CryptoCompareClient,
    private val jsonBridge: JsonBridge
) {

    private val maxReqPerSecond = 20
    private val maxReqPerSecondDelay = 1000L / maxReqPerSecond
    private var lastRequestMark = LoadMark(0, 0)
    private val location = File(Locations.Daily)

    suspend fun loadAll(assets: Collection<Asset>): Map<Asset, Result<List<PriceItem>>> = assets
        .associateWith { asset ->
            kotlin.runCatching { load(asset) }
                .onFailure {
                    System.err.println("Unable to load asset:$asset\n${it.message}\n${it.stackTraceToString()}")
                }
        }

    suspend fun load(asset: Asset): List<PriceItem> {
        location.mkdirs()
        val f = File(location, "${asset.label}.json")

        var result: List<PriceItem>? = null
        if (f.existsAndHasSize()) {
            result =
                jsonBridge.deserialize<List<CryptoComparePriceItem>>(f.readText()).mapIndexed { index, cryptoComparePriceItem -> PriceItem(index, asset, cryptoComparePriceItem) }

            //in case of some data mess, just delete it and redownload it fully
            val anyDataGap = result.zipWithNext().any { (x, y) -> x.dateTime.date.daysUntil(y.dateTime.date) > 1 }
            if (anyDataGap) {
                result = null
                f.delete()
            }
        }

        val lastLoadedDate = result?.lastOrNull()?.dateTime?.date
        val toLoad = lastLoadedDate?.daysUntil(now().date) ?: 1000
        if (toLoad > 0) {
            slowDownIfNecessary()
            val startIndex = result?.size ?: 0
            val update = cryptoCompareClient.getHistoryData(asset.coin1, asset.coin2, limit = toLoad).data.items
                .run {
                    if (result != null) drop(1) else this
                }
                .also { f.writeText(jsonBridge.serialize(it)) }
                .mapIndexed { index, cryptoComparePriceItem -> PriceItem(startIndex + index, asset, cryptoComparePriceItem) }
            result = (result ?: emptyList()) + update
        }

        return requireNotNull(result) {
            "Nothing has been loaded for asset:${asset}"
        }
    }

    private suspend fun slowDownIfNecessary() {
        val now = System.currentTimeMillis()
        val sinceLastLoadDiff = now - lastRequestMark.timeStamp
        if (sinceLastLoadDiff < 1000) {
            lastRequestMark.counter++
            val delay = when {
                lastRequestMark.counter < 3 -> 0L
                lastRequestMark.counter < maxReqPerSecond / 2 -> maxReqPerSecondDelay / 2
                else -> (maxReqPerSecondDelay * 1.5).toLong()
            }
            println("LoadPricesDelay:$delay")
            delay(delay)
        } else {
            lastRequestMark.timeStamp = now
            lastRequestMark.counter = 1
        }
    }
}