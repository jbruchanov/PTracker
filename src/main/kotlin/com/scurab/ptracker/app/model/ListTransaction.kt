package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.allCoins
import com.scurab.ptracker.app.ext.setOf

fun List<Transaction>.tradingAssets(primaryFiatCoin: String? = null): List<Asset> {
    val allAssets = setOf { it.asset }
    val allCoins = allAssets.allCoins()
    val allTradingAssets = allAssets.filter { it.isTradingAsset }
    val allTradingCoins = allTradingAssets.allCoins()
    val (missedFiat, missedCrypto) = (allCoins - allTradingCoins).partition { FiatCurrencies.contains(it) }
    val missedAssets =
        primaryFiatCoin?.let { primaryFiatCoin -> (missedFiat + primaryFiatCoin).map { f -> missedCrypto.map { c -> Asset(c, f) } } }?.flatten() ?: emptyList()
    return (allTradingAssets + missedAssets).sorted()
}