package com.scurab.ptracker.app.model

import java.io.File
import java.math.BigDecimal

interface ValueContainer<T> {
    val item: T
}

interface ValueWithLocalIcon<T> : ValueContainer<T> {
    fun icon(): File = File(Locations.Icons, item.toString().lowercase() + ".png")
}

data class CoinCalculation<T>(val key: T, val value: BigDecimal)

@JvmInline
value class AnyCoin(override val item: String) : ValueContainer<String>, ValueWithLocalIcon<String> {
    fun isFiat() = FiatCurrencies.contains(item)
}

@JvmInline
value class FiatCoin(override val item: String) : ValueContainer<String>, ValueWithLocalIcon<String>

@JvmInline
value class CryptoCoin(override val item: String) : ValueContainer<String>, ValueWithLocalIcon<String>

@JvmInline
value class ExchangeWallet(override val item: String) : ValueContainer<String>

class LedgerStats(
    val assets: List<Asset>,
    val assetsByExchange: Map<ExchangeWallet, List<Asset>>,
    val feesPerCoin: Map<String, BigDecimal>,
    val cryptoHoldings: Map<Asset, CryptoHoldings>,
    val coinSumPerExchange: Map<String, List<CoinExchangeStats>>,
    val exchangeSumOfCoins: Map<ExchangeWallet, List<CoinCalculation<AnyCoin>>>,
    val transactionsPerAssetPerType: List<Pair<String, List<Pair<Asset, Pair<BigDecimal, BigDecimal>>>>>
) {
    companion object {
        val Empty = LedgerStats(emptyList(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyList())
    }
}



