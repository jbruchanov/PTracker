package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.firstIf
import com.scurab.ptracker.app.ext.other
import com.scurab.ptracker.app.serialisation.AssetAsStringSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable(with = AssetAsStringSerializer::class)
data class Asset(val coin1: String, val coin2: String) : Comparable<Asset> {
    fun has(value: String) = coin1 == value || coin2 == value
    fun has(value1: String, value2: String) = (value1 == coin1 && value2 == coin2) || (value2 == coin1 && value1 == coin2)
    fun contains(value1: String, value2: String) = has(value1) || has(value2)

    @Transient
    val label by lazy {
        buildString {
            append(coin1)
            if (isNotEmpty() && coin2.isNotEmpty()) {
                append("-")
            }
            append(coin2)
        }
    }

    //getters seem to be necessary for deserialized objects
    @Transient
    val isEmpty
        get() = this == Empty

    @Transient
    val isNotEmpty
        get() = this != Empty

    @Transient
    val isTradingAsset by lazy { coin1.isNotEmpty() && coin2.isNotEmpty() }

    @Transient
    val isCryptoTradingAsset by lazy { isTradingAsset && !(FiatCurrencies.contains(coin1) && FiatCurrencies.contains(coin2)) }

    @Transient
    val isFiatTradingAsset by lazy { isTradingAsset && FiatCurrencies.contains(coin1) && FiatCurrencies.contains(coin2) }

    val isIdentity get() = coin1 == coin2

    fun cryptoLabelOnlyIf(value: Boolean) = if (value) coin1 else label

    fun iconCoin1() = File(Locations.Icons, coin1.lowercase() + ".png")

    fun fiatCoin(): FiatCoin = (coin2.takeIf { FiatCurrencies.contains(coin2) } ?: coin1.takeIf { FiatCurrencies.contains(coin1) })
        ?.let { FiatCoin(it) }
        ?: throw IllegalStateException("No fiat coin in $this")

    fun cryptoCoin(): CryptoCoin = (coin2.takeIf { !FiatCurrencies.contains(coin2) } ?: coin1.takeIf { !FiatCurrencies.contains(coin1) })
        ?.let { CryptoCoin(it) }
        ?: throw IllegalStateException("No crypto coin in $this")

    fun fiatCoinOrNull() = kotlin.runCatching { fiatCoin() }.getOrNull()
    fun cryptoCoinOrNull() = kotlin.runCatching { cryptoCoin() }.getOrNull()

    fun flipCoins(): Asset = Asset(coin2, coin1)

    fun exchangeFiatAsset(coin: String): Asset {
        val isCoin1Fiat = FiatCurrencies.contains(coin1)
        val isCoin2Fiat = FiatCurrencies.contains(coin2)
        return when {
            isCoin1Fiat && !isCoin2Fiat -> Asset(coin1, coin)
            !isCoin1Fiat && isCoin2Fiat -> Asset(coin2, coin)
            else -> throw IllegalStateException("Invalid asset for replacement:$this, can't replace coin:$coin")
        }
    }

    override fun toString(): String = "$coin1-$coin2"

    companion object {
        val Empty = Asset("", "")

        fun fromUnknownPair(coin1: String?, coin2: String?): Asset {
            requireNotNull(coin1) { "Coin1 is null" }
            requireNotNull(coin2) { "Coin2 is null" }
            require(coin1 != coin2) { "Invalid pair:$coin1, $coin2" }
            val isCoin1Fiat = FiatCurrencies.contains(coin1)
            //currently ignored, for some Fiat/Fiat exchange data, so the Asset crypto/Fiat won't work in some edgecase
            //val isCoin2Fiat = FiatCurrencies.contains(coin2)
            //require((isCoin1Fiat xor isCoin2Fiat)) { "Invalid pair:$coin1, $coin2" }
            val pair = Pair(coin1, coin2)
            val crypto = pair.firstIf(!isCoin1Fiat)
            return Asset(
                coin1 = pair.firstIf(!isCoin1Fiat),
                coin2 = pair.other(crypto)
            )
        }

        fun fromUnknownPairOrNull(coin1: String?, coin2: String?) = kotlin.runCatching { fromUnknownPair(coin1, coin2) }.getOrNull()

        fun fromString(value: String): Asset {
            require(value.isNotEmpty())
            val items = value.split("-")
            require(items.size == 2) { "Invalid input:${value}, must be 'COIN1-COIN2'" }
            return fromUnknownPair(items[0], items[1])
        }

        fun fromStringOrEmpty(value: String): Asset = kotlin.runCatching { fromString(value) }.getOrElse { Empty }
    }

    override fun compareTo(other: Asset): Int = label.compareTo(other.label)
}