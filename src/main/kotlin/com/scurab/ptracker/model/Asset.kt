package com.scurab.ptracker.model

import java.io.File

data class Asset(val crypto: String, val fiat: String) {
    fun has(value: String) = crypto == value || fiat == value
    fun has(value1: String, value2: String) = (value1 == crypto && value2 == fiat) || (value2 == crypto && value1 == fiat)
    val label = buildString {
        append(crypto)
        if (isNotEmpty() && fiat.isNotEmpty()) {
            append("-")
        }
        append(fiat)
    }

    override fun toString(): String = label

    val isEmpty = this == Empty

    fun iconCrypto() = File(Locations.Icons, crypto.lowercase() + ".png")

    companion object {
        val Empty = Asset("", "")

        fun fromUnknownPair(coin1: String, coin2: String): Asset {
            require(coin1 != coin2) { "Invalid pair:$coin1, $coin2" }
            val isCoin1Fiat = FiatCurrencies.contains(coin1)
            val isCoin2Fiat = FiatCurrencies.contains(coin2)
            require((isCoin1Fiat xor isCoin2Fiat)) { "Invalid pair:$coin1, $coin2" }
            return Asset(
                crypto = coin1.takeIf { !isCoin1Fiat } ?: coin2,
                fiat = coin1.takeIf { isCoin1Fiat } ?: coin2
            )
        }

        fun fromUnknownPairOrNull(coin1: String, coin2: String) = kotlin.runCatching { fromUnknownPair(coin1, coin2) }.getOrNull()
    }
}