package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.AnyCoin
import com.scurab.ptracker.app.model.CoinCalculation
import com.scurab.ptracker.app.model.CryptoCoin
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.HasIncome
import com.scurab.ptracker.app.model.HasOutcome
import com.scurab.ptracker.app.model.Holdings
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.Transaction
import java.math.BigDecimal

class StatsCalculatorUseCase {

    fun calculateStats(ledger: Ledger, filter: Filter<Transaction>): LedgerStats {
        //predata
        val data = ledger.items.filter(filter)
        val allCoins = data.map { it.assets }.flatten().toSet()
        val fiatCoins = allCoins.filter { FiatCurrencies.contains(it) }.toSet()
        val cryptoCoins = allCoins - fiatCoins
        val tradingAssets = data.map { it.asset }.filter { it.isCryptoTradingAsset }.toSet()
        val exchanges = data.map { it.exchange }.toSet()
        val transactionTypes = data.map { it.type }.toSet()
        val transactionsByExchange = data.groupBy { it.exchange }
        val assetsByExchange = exchanges
            .mapNotNull { exchange -> exchange.normalizedExchange()?.let { normalized -> exchange to normalized }}
            .associateBy(
                keySelector = { ExchangeWallet(it.second) },
                valueTransform = { (exchange, normalized) ->
                    data.asSequence()
                        .filterIsInstance<Transaction.Trade>()
                        .filter { it.exchange == exchange }
                        .filter { it.asset.isCryptoTradingAsset }
                        .map { it.asset }.toSet().sorted()
                })

        //currently, what I have on exchange/wallet
        val actualOwnership = tradingAssets.associateWith { asset -> CoinCalculation(asset, ledger.items.filter { it.hasAsset(asset) }.sumOf { it.getAmount(asset.crypto) }) }
        //traded amounts => values what I'd have had if without losts/gifts/etc => value used for price per unit
        val tradedAmount =
            tradingAssets.associateWith { asset ->
                CoinCalculation(
                    asset,
                    ledger.items.filterIsInstance<Transaction.Trade>().filter { it.hasAsset(asset) }.sumOf { it.getAmount(asset.crypto) })
            }
        val spentFiatByCrypto = tradingAssets.associateWith { asset ->
            CoinCalculation(CryptoCoin(asset.crypto), data.filter { it.hasAsset(asset) }.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(asset.fiat) })
        }
        val holdings = tradingAssets.associateWith { asset ->
            Holdings(
                asset,
                actualOwnership.getValue(asset).value,
                tradedAmount.getValue(asset).value,
                spentFiatByCrypto.getValue(asset).value.abs()
            )
        }

        val exchangeSumOfCoins = transactionsByExchange.map { (exchange, transactions) ->
            ExchangeWallet(exchange) to allCoins.map { anyCoin -> CoinCalculation(AnyCoin(anyCoin), transactions.sumOf { it.getAmount(anyCoin) }) }
        }.toMap()

        val transactionsPerAssetPerType = transactionTypes.map { type -> type to data.filter { it.type == type } }
            .map { (type, transactions) -> Triple(type, transactions.map { it.asset }.toSet(), transactions) }.map { (type, assets, transactions) ->
                type to assets.map { asset ->
                    asset to transactions.filter { it.hasAsset(asset) }.let { ts -> ts.sumOf { it.getAmount(asset.fiat) } to ts.sumOf { it.getAmount(asset.crypto) } }
                }
            }

        return LedgerStats(tradingAssets.toList(), assetsByExchange, holdings, exchangeSumOfCoins, transactionsPerAssetPerType)
    }

    private fun String.normalizedExchange(): String? {
        val name = this.lowercase()
        return when {
            name.contains("kraken") -> "Kraken"
            name.contains("coinbase") -> "Coinbase"
            name.contains("binance") -> "Binance"
            name.contains("trezor") -> null
            else -> null
        }
    }
}

fun Transaction.getAmount(asset: String): BigDecimal {
    val fee = if (feeAsset == asset) -feeQuantity else BigDecimal.ZERO
    return fee + when {
        this is HasIncome && buyAsset == asset -> buyQuantity
        this is HasOutcome && sellAsset == asset -> -sellQuantity
        else -> BigDecimal.ZERO
    }
}