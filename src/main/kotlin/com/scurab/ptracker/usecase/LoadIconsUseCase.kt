package com.scurab.ptracker.usecase

import com.scurab.ptracker.ext.parallelMapIndexed
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.FiatCurrencies
import com.scurab.ptracker.model.Locations
import com.scurab.ptracker.net.CryptoCompareClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import java.io.File

class LoadIconsUseCase(
    private val httpClient: HttpClient,
    private val cryptoCompareClient: CryptoCompareClient
) {

    private val location = File(Locations.Icons)

    suspend fun loadIcons(cryptoAssets: List<Asset>): List<Pair<String, File?>> {
        val allCoins = (cryptoAssets.map { it.fiat } + cryptoAssets.map { it.crypto }).toSet()
        location.mkdirs()

        val result = allCoins
            .map { c -> c to File(location, "${c.lowercase()}.png") }
            .filterNot { (_, f) -> f.exists() && f.length() > 0L }
            .parallelMapIndexed { _, (c, f) ->
                val fullImageUrl = if (FiatCurrencies.contains(c)) {
                    "${Locations.IconsUrl}${c.take(2).lowercase()}.png"
                } else {
                    kotlin.runCatching { cryptoCompareClient.getCoinData(c).data[c]?.fullImageUrl }.getOrNull()
                }
                fullImageUrl?.let { kotlin.runCatching { httpClient.get<HttpResponse>(it).content.copyTo(f.writeChannel()) }.getOrNull() }
                Triple(c, f, fullImageUrl)
            }
        val downloadsMap = result.associateBy(keySelector = { it.first }, valueTransform = { it.second })
        return allCoins.map { it to downloadsMap[it]?.takeIf { f -> f.exists() && f.length() > 0 } }
    }
}