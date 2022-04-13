package com.scurab.ptracker.app.ext

import androidx.compose.ui.graphics.Color
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.ui.CoinColors
import com.scurab.ptracker.ui.RandomColors

fun List<Asset>.colors(): List<Color> {
    var colorIndex = 0
    return map { CoinColors[it.coin1] ?: CoinColors[it.coin2] ?: RandomColors[colorIndex++ % size] }
}

fun Collection<Asset>.allCoins() = (map { it.coin1 } + map { it.coin2 }).distinct().filter { it.isNotBlank() }
fun Collection<Asset>.fiatCoins() = allCoins().filter { FiatCurrencies.contains(it) }
fun Collection<Asset>.withPrimaryCoin(primaryCoin: String?): Collection<Asset> {
    if (primaryCoin == null) return this
    return (this.toSet() + this.allCoins().map { Asset(it, primaryCoin) })
}