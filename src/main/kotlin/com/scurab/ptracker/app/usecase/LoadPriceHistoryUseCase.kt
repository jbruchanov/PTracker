package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.existsAndHasSize
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.toLong
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
    private val hourInMs = 60 * 60 * 1000L

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
        var localData: List<CryptoComparePriceItem>? = null
        val now = now()
        if (f.existsAndHasSize()) {
            localData = jsonBridge.deserialize<List<CryptoComparePriceItem>>(f.readText())
                .run {
                    //don't download data which are downloaded in last [hourInMs] only
                    val timeDiffSinceNow = lastOrNull()?.timeMs?.let { now.toLong() - it } ?: 0
                    if (timeDiffSinceNow > hourInMs) dropLast(1)
                    else this
                }

            //in case of some data mess, just delete it and redownload it fully
            val anyDataGap = localData.zipWithNext().any { (x, y) -> x.dateTime.date.daysUntil(y.dateTime.date) > 1 }
            if (anyDataGap) {
                f.delete()
                localData = null
            }
        }

        val lastLoadedDate = localData?.lastOrNull()?.dateTime?.date
        val toLoad = lastLoadedDate?.daysUntil(now.date) ?: 1000
        val loadedData: List<CryptoComparePriceItem> = if (toLoad > 0) {
            slowDownIfNecessary()
            cryptoCompareClient.getHistoryData(asset.coin1, asset.coin2, limit = toLoad).data.items
                .run {
                    if (localData != null) drop(1) else this
                }
                .toMutableList()
                .also {
                    //overwrite the timestamp of the data to the time actually downloaded so we know if we need to refresh it
                    if (it.isNotEmpty()) {
                        it[it.size - 1] = it.last().copy(time = now().toLong() / 1000)
                    }
                }
        } else emptyList()

        return ((localData ?: emptyList()) + loadedData)
            .also {
                if (toLoad > 0) {
                    f.writeText(jsonBridge.serialize(it))
                }
            }
            .mapIndexed { index, cryptoComparePriceItem -> PriceItem(index, asset, cryptoComparePriceItem) }
    }

    private suspend fun slowDownIfNecessary() {
        val now = System.currentTimeMillis()
        val sinceLastLoadDiff = now - lastRequestMark.timeStamp
        if (sinceLastLoadDiff < 2000) {
            lastRequestMark.counter++
            val delay = when {
                lastRequestMark.counter < maxReqPerSecond / 2 -> maxReqPerSecondDelay
                else -> (maxReqPerSecondDelay * 2L)
            }
            println("LoadPricesDelay:$delay")
            delay(delay)
        } else {
            lastRequestMark.timeStamp = now
            lastRequestMark.counter = 1
        }
    }
}