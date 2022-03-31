package com.scurab.ptracker.usecase

import com.scurab.ptracker.model.AnyCoin
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.CoinCalculation
import com.scurab.ptracker.model.CryptoCoin
import com.scurab.ptracker.model.ExchangeWallet
import com.scurab.ptracker.model.FiatCurrencies
import com.scurab.ptracker.model.Filter
import com.scurab.ptracker.model.HasIncome
import com.scurab.ptracker.model.HasOutcome
import com.scurab.ptracker.model.Holdings
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.LedgerStats
import com.scurab.ptracker.model.Transaction
import java.math.BigDecimal

class StatsCalculatorUseCase {

    fun calculateStats(ledger: Ledger, filter: Filter<Transaction>): LedgerStats {

        //predata
        val data = ledger.items.filter(filter)
        val allCoins = data.map { it.assets }.flatten().toSet()
        val fiatCoins = allCoins.filter { FiatCurrencies.contains(it) }.toSet()
        val cryptoCoins = allCoins - fiatCoins
        val assets = cryptoCoins.map { c -> fiatCoins.map { f -> Asset(c, f) } }.flatten()
        val exchanges = data.map { it.exchange }.toSet()
        val transactionTypes = data.map { it.type }.toSet()
        val transactionsByExchange = data.groupBy { it.exchange }

        //currently, what I have on exchange/wallet
        val actualOwnership = allCoins.associateWith { anyCoin -> CoinCalculation(AnyCoin(anyCoin), ledger.items.sumOf { it.getAmount(anyCoin) }) }
        //traded amounts => values what I'd have had if without losts/gifts/etc => value used for price per unit
        val tradedAmount =
            cryptoCoins.associateWith { crypto -> CoinCalculation(CryptoCoin(crypto), ledger.items.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(crypto) }) }
        val spentFiatByCrypto = assets.associateWith { asset ->
            CoinCalculation(CryptoCoin(asset.crypto), data.filter { it.hasAsset(asset) }.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(asset.fiat) })
        }
        val holdings = assets.map { asset ->
            Holdings(
                asset,
                actualOwnership.getValue(asset.crypto).value,
                tradedAmount.getValue(asset.crypto).value,
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

        return LedgerStats(assets, holdings, exchangeSumOfCoins, transactionsPerAssetPerType)
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