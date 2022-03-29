package com.scurab.ptracker.usecase

import com.scurab.ptracker.ext.parallelMapIndexed
import com.scurab.ptracker.model.Asset
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

    suspend fun loadIconsForCryptoCoins(assets: Collection<Asset>): List<Pair<String, File?>> = loadIcons(assets.map { it.crypto }.distinct())

    suspend fun loadIcons(cryptoAssets: Collection<String>): List<Pair<String, File?>> {
        location.mkdirs()
        val downloads = cryptoAssets
            .map { c -> c to File(location, "${c}.png") }
            .filterNot { (_, f) -> f.exists() && f.length() > 0L }
            .parallelMapIndexed { _, (c, f) ->
                val fullImageUrl = kotlin.runCatching { cryptoCompareClient.getCoinData(c).data[c]?.fullImageUrl }.getOrNull()
                Triple(c, f, fullImageUrl)
            }
            .parallelMapIndexed { i, triple ->
                val (_, f, url) = triple
                url?.let { httpClient.get<HttpResponse>(url).content.copyTo(f.writeChannel()) }
                triple
            }
        val downloadsMap = downloads.associateBy(keySelector = { it.first }, valueTransform = { it.second })
        return cryptoAssets.map { it to downloadsMap[it]?.takeIf { f -> f.exists() && f.length() > 0 } }
    }
}