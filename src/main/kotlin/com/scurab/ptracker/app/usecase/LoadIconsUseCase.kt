package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.existsAndHasSize
import com.scurab.ptracker.app.ext.parallelMapIndexed
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.net.CryptoCompareClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyTo
import java.io.File

class LoadIconsUseCase(
    private val appSettings: AppSettings,
    private val httpClient: HttpClient,
    private val cryptoCompareClient: CryptoCompareClient
) {

    private val location = File(Locations.Icons)

    suspend fun loadIcons(assets: Collection<Asset>): List<Pair<String, File?>> {
        val primaryCoin = appSettings.primaryCoin?.let { listOf(it) } ?: emptyList()
        val allCoins = (assets.map { it.coin2 } + assets.map { it.coin1 } + primaryCoin).toSet()
        location.mkdirs()

        val result = allCoins
            .map { c -> c to File(location, "${c.lowercase()}.png") }
            .filterNot { (_, f) -> f.existsAndHasSize() }
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
        return allCoins.map { it to downloadsMap[it]?.takeIf { f -> f.existsAndHasSize() } }
    }
}