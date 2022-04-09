package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.ext.convertTradePrice
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.model.AnyCoin
import com.scurab.ptracker.app.model.CoinCalculation
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.CryptoCoin
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.HasIncome
import com.scurab.ptracker.app.model.HasOutcome
import com.scurab.ptracker.app.model.CryptoHoldings
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.repository.AppSettings
import java.math.BigDecimal

class StatsCalculatorUseCase(
    private val appSettings: AppSettings
) {

    fun calculateStats(
        ledger: Ledger,
        filter: Filter<Transaction>,
        prices: List<MarketPrice> = emptyList(),
        primaryCurrency: String? = appSettings.primaryCoin
    ): LedgerStats {
        //predata
        val pricesMap = prices.associateBy { it.asset }
        val data = ledger.items.filter(filter).let { data ->
            if (primaryCurrency == null) data else data.map { transaction ->
                transaction.convertTradePrice(pricesMap, primaryCurrency)
            }
        }
        val allCoins = data.map { it.assets }.flatten().toSet()
        val fiatCoins = allCoins.filter { FiatCurrencies.contains(it) }.toSet()
        val cryptoCoins = allCoins - fiatCoins
        val cryptoTradingAssets = data.setOf { it.asset }.filter { it.isCryptoTradingAsset }
        val exchanges = data.setOf { it.exchange }
        val transactionTypes = data.setOf { it.type }
        val transactionsByExchange = data.groupBy { it.exchange }
        val sumOfCoins = allCoins.associateWith { coin ->
            data.filter { it.asset.has(coin) }.map { it.getAmount(coin) }.sumOf { it }
        }
        val feesPerCoin = allCoins.associateWith { coin -> data.sumOf { transaction -> transaction.getFees(coin) } }
        val assetsByExchange = exchanges
            .mapNotNull { exchange -> exchange.normalizedExchange()?.let { normalized -> exchange to normalized } }
            .associateBy(
                keySelector = { ExchangeWallet(it.second) },
                valueTransform = { (exchange, normalized) ->
                    data.asSequence()
                        .filterIsInstance<Transaction.Trade>()
                        .filter { it.exchange == exchange }
                        .filter { it.asset.isTradingAsset }
                        .map { it.asset }.toSet().sorted()
                })

        //currently, what I have on exchange/wallet
        val actualOwnership = cryptoTradingAssets.associateWith { asset -> CoinCalculation(asset, data.filter { it.hasAsset(asset) }.sumOf { it.getAmount(asset.coin1) }) }
        //traded amounts => values what I'd have had if without losts/gifts/etc => value used for price per unit
        val tradedAmount =
            cryptoTradingAssets.associateWith { asset ->
                CoinCalculation(
                    asset,
                    data.filterIsInstance<Transaction.Trade>().filter { it.hasAsset(asset) }.sumOf { it.getAmount(asset.coin1) })
            }
        val spentFiatByCrypto = cryptoTradingAssets.associateWith { asset ->
            CoinCalculation(CryptoCoin(asset.coin1), data.filter { it.hasAsset(asset) }.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(asset.coin2) })
        }
        val cryptoHoldings = cryptoTradingAssets.associateWith { asset ->
            CryptoHoldings(
                asset,
                actualOwnership.getValue(asset).value,
                tradedAmount.getValue(asset).value,
                spentFiatByCrypto.getValue(asset).value.abs(),
                feesPerCoin[asset.cryptoCoinOrNull()?.item] ?: ZERO
            )
        }

        val exchangeSumOfCoins = transactionsByExchange
            .mapNotNull { (exchange, transactions) ->
                ExchangeWallet(exchange) to allCoins
                    .map { anyCoin -> CoinCalculation(AnyCoin(anyCoin), transactions.sumOf { it.getAmount(anyCoin) }) }
                    .filter { it.value.isNotZero() }
            }
            .filter { it.second.isNotEmpty() }
            .toMap()

        val transactionsPerAssetPerType = transactionTypes.map { type -> type to data.filter { it.type == type } }
            .map { (type, transactions) -> Triple(type, transactions.map { it.asset }.toSet(), transactions) }.map { (type, assets, transactions) ->
                type to assets.map { asset ->
                    asset to transactions.filter { it.hasAsset(asset) }.let { ts -> ts.sumOf { it.getAmount(asset.coin2) } to ts.sumOf { it.getAmount(asset.coin1) } }
                }
            }

        val coinSumPerExchange = allCoins.associateWith { coin ->
            data.filter { it.asset.has(coin) }
                .groupBy { it.exchange }
                .mapValues { (_, transactions) -> transactions.sumOf { transaction -> transaction.getAmount(coin) } }
                .filter { it.value.isNotZero() }
                .map { (exchange, sum) -> CoinExchangeStats(AnyCoin(coin), ExchangeWallet(exchange), sum, sum.safeDiv(sumOfCoins.getValue(coin))) }
        }

        return LedgerStats(cryptoTradingAssets.toList(), assetsByExchange, feesPerCoin, cryptoHoldings, coinSumPerExchange, exchangeSumOfCoins, transactionsPerAssetPerType)
    }

    private fun String.normalizedExchange(): String? {
        return when {
            contains("kraken", ignoreCase = true) -> "Kraken"
            contains("coinbase", ignoreCase = true) -> "Coinbase"
            contains("binance", ignoreCase = true) -> "Binance"
            contains("trezor", ignoreCase = true) -> null
            else -> null
        }
    }
}

fun Transaction.getAmount(asset: String): BigDecimal {
    val fee = if (feeAsset == asset) -feeQuantity else ZERO
    return fee + when {
        this is HasIncome && buyAsset == asset -> buyQuantity
        this is HasOutcome && sellAsset == asset -> -sellQuantity
        else -> ZERO
    }
}

fun Transaction.getFees(asset: String): BigDecimal = if (feeAsset == asset) -feeQuantity else ZERO
