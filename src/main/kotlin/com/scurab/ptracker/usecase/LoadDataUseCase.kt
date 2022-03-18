package com.scurab.ptracker.usecase

import com.scurab.ptracker.model.CryptoComparePriceItem
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.randomPriceData
import com.scurab.ptracker.serialisation.JsonBridge
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

class LoadDataUseCase {
    suspend fun loadData(item: String): List<PriceItem> {
        val f = File("data/$item.json")
        val items = if (f.exists()) {
            JsonBridge.deserialize<List<CryptoComparePriceItem>>(f.readText()).mapIndexed { index, cryptoComparePriceItem -> PriceItem(index, cryptoComparePriceItem) }
        } else {
            randomPriceData(Random, 100, Clock.System.now().minus(1000L.days).toLocalDateTime(TimeZone.UTC), 1L.days)
        }
        return items
    }
}