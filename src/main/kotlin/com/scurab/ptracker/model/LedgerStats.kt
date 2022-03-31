package com.scurab.ptracker.model

import java.math.BigDecimal

interface ValueContainer<T> {
    val item: T
}

data class CoinCalculation<T>(val key: T, val value: BigDecimal)

@JvmInline
value class AnyCoin(override val item: String) : ValueContainer<String>

@JvmInline
value class FiatCoin(override val item: String) : ValueContainer<String>

@JvmInline
value class CryptoCoin(override val item: String) : ValueContainer<String>

@JvmInline
value class ExchangeWallet(override val item: String) : ValueContainer<String>

class LedgerStats(
    val assets: List<Asset>,
    val holdinds: List<Holdings>,
    val exchangeSumOfCoins: Map<ExchangeWallet, List<CoinCalculation<AnyCoin>>>,
    val transactionsPerAssetPerType: List<Pair<String, List<Pair<Asset, Pair<BigDecimal, BigDecimal>>>>>
)



