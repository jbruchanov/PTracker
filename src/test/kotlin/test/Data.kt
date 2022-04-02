package test

import com.scurab.ptracker.model.Asset

val SampleFiatCoins = listOf("USD", "GBP")
val SampleCryptoCoins = listOf("BTC", "ETH", "LTC", "ADA", "DOT", "AVAX", "AAVE", "DOGE")
val TestCoins = SampleFiatCoins.map { fiat -> SampleCryptoCoins.map { crypto -> Asset(crypto, fiat) } }.flatten()