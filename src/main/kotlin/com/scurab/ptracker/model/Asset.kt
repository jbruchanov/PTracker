package com.scurab.ptracker.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
data class Asset(val crypto: String, val fiat: String) : Comparable<Asset> {
    fun has(value: String) = crypto == value || fiat == value
    fun has(value1: String, value2: String) = (value1 == crypto && value2 == fiat) || (value2 == crypto && value1 == fiat)

    @Transient
    val label = buildString {
        append(crypto)
        if (isNotEmpty() && fiat.isNotEmpty()) {
            append("-")
        }
        append(fiat)
    }

    @Transient
    val isEmpty = this == Empty

    @Transient
    val isCryptoTradingAsset = crypto.isNotEmpty() && fiat.isNotEmpty()/* && !FiatCurrencies.contains(crypto) && FiatCurrencies.contains(fiat)*/

    fun cryptoLabelOnlyIf(value: Boolean) = if (value) crypto else label

    fun iconCrypto() = File(Locations.Icons, crypto.lowercase() + ".png")

    fun ensureFiatOrNull() = fiat.takeIf { FiatCurrencies.contains(fiat) } ?: crypto.takeIf { FiatCurrencies.contains(crypto) }

    override fun toString(): String = label

    companion object {
        val Empty = Asset("", "")

        fun fromUnknownPair(coin1: String?, coin2: String?): Asset {
            requireNotNull(coin1) { "Coin1 is null" }
            requireNotNull(coin2) { "Coin2 is null" }
            require(coin1 != coin2) { "Invalid pair:$coin1, $coin2" }
            val isCoin1Fiat = FiatCurrencies.contains(coin1)
            val isCoin2Fiat = FiatCurrencies.contains(coin2)
            require((isCoin1Fiat xor isCoin2Fiat)) { "Invalid pair:$coin1, $coin2" }
            return Asset(
                crypto = coin1.takeIf { !isCoin1Fiat } ?: coin2,
                fiat = coin1.takeIf { isCoin1Fiat } ?: coin2
            )
        }

        fun fromUnknownPairOrNull(coin1: String?, coin2: String?) = kotlin.runCatching { fromUnknownPair(coin1, coin2) }.getOrNull()
    }

    override fun compareTo(other: Asset): Int = label.compareTo(other.label)
}