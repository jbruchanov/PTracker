package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.app.model.MarketData
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.Transaction

fun List<Transaction>.tradingAssets(primaryFiatCoin: String? = null): List<Asset> {
    val allAssets = setOf { it.asset }
    val allCoins = allAssets.allCoins()
    val allTradingAssets = allAssets.filter { it.isTradingAsset }
    val allTradingCoins = allTradingAssets.allCoins()
    val (missedFiat, missedCrypto) = (allCoins - allTradingCoins).partition { FiatCurrencies.contains(it) }
    val missedAssets = primaryFiatCoin
        ?.let { coin ->
            (missedFiat + coin)
                .filter { it == coin }
                .map { f -> missedCrypto.map { c -> Asset(c, f) } }
        }
        ?.flatten() ?: emptyList()
    return (allTradingAssets + missedAssets).sorted()
}

fun List<Transaction>.totalMarketValue(prices: Map<Asset, MarketPrice>, primaryCurrency: String): MarketData {
    if (isEmpty()) return MarketData.Empty
    return map { transaction ->
        val coinValues = transaction.coinValues()
        var sumValue = 0.bd
        var sumCost = 0.bd
        coinValues.map { (coin, value) ->
            val asset = Asset.fromUnknownPair(coin, primaryCurrency)
            val price = when {
                asset.isIdentity -> 1.bd
                else -> requireNotNull(prices[asset] ?: prices[asset.flipCoins()]) { "Missing price for asset:$asset" }.price
            }
            val fiatValue = (price * value.abs())
            coin to fiatValue
        }.forEach { (originalCoin, fiatValue) ->
            val isCoinCrypto = !FiatCurrencies.contains(originalCoin)
            when {
                /*
                Income:
                    +BTC -> value grows, cost 0
                    +GBP -> value 0, cost 0
                 */
                transaction is Transaction.Income && isCoinCrypto -> sumValue += fiatValue
                /*
                Outcome:
                    -BTC -> value declines, cost 0
                    -GBP -> value 0, cost 0
                 */
                transaction is Transaction.Outcome && isCoinCrypto -> sumValue -= fiatValue
                /*
                Trade Buy ->
                  1) +BTC => value grows
                  2) -GBP => cost grows
                Trade Sell ->
                  3) -BTC => value declines
                  4) +GBP => cost declines
                 */
                //1 & 3
                transaction is Transaction.Trade && isCoinCrypto -> sumValue += (transaction.isCryptoBuy.signBd() * fiatValue)
                //2 & 4
                transaction is Transaction.Trade && !isCoinCrypto -> sumCost += (transaction.isCryptoBuy.signBd() * fiatValue)
            }
        }

        sumCost to sumValue
    }.let { coinStats ->
        MarketData(coinStats.sumOf { it.first }, coinStats.sumOf { it.second })
    }
}

fun List<Transaction.Trade>.coinSum(coin: String) = map { it.getAmount(coin) }
    .partition { !it.isNegative }
    .let { (l, r) -> Pair(l.sumOf { it }, r.sumOf { it }) }