package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.convertTradePrice
import com.scurab.ptracker.app.ext.getAmount
import com.scurab.ptracker.app.ext.getFees
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.ext.totalMarketValue
import com.scurab.ptracker.app.ext.tradingAssets
import com.scurab.ptracker.app.model.AnyCoin
import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinCalculation
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.CryptoCoin
import com.scurab.ptracker.app.model.CryptoHoldings
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.GroupStatsSum
import com.scurab.ptracker.app.model.GroupStrategy
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.MarketData
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.repository.AppSettings
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import java.util.TreeMap

class StatsCalculatorUseCase(
    private val appSettings: AppSettings
) {

    fun calculateStats(
        ledger: Ledger, filter: Filter<Transaction>, prices: Map<Asset, MarketPrice> = emptyMap(), primaryCurrency: String? = appSettings.primaryCoin
    ): LedgerStats {
        //predata
        val data = ledger.items.filter(filter).let { data ->
            if (primaryCurrency == null) data else data.map { transaction ->
                transaction.convertTradePrice(prices, primaryCurrency)
            }
        }
        val allCoins = data.map { it.assets }.flatten().toSet()
        val fiatCoins = allCoins.filter { FiatCurrencies.contains(it) }.toSet()
        val cryptoCoins = allCoins - fiatCoins
        val tradingAssets = data.tradingAssets(primaryCurrency)
        val exchanges = data.setOf { it.exchange }
        val transactionTypes = data.setOf { it.type }
        val transactionsByExchange = data.groupBy { it.exchange }
        val sumOfCoins = allCoins.associateWith { coin ->
            data.filter { it.asset.has(coin) }.map { it.getAmount(coin) }.sumOf { it }
        }
        val feesPerCoin = allCoins.associateWith { coin -> data.sumOf { transaction -> transaction.getFees(coin) } }
        val assetsByExchange = exchanges.mapNotNull { exchange -> exchange to exchange.normalizedExchange() }
            .associateBy(keySelector = { ExchangeWallet(it.second) }, valueTransform = { (exchange, normalized) ->
                data.asSequence().filterIsInstance<Transaction.Trade>().filter { it.exchange == exchange }.filter { it.asset.isTradingAsset }.map { it.asset }.toSet().sorted()
            })

        //currently, what I have on exchange/wallet
        val actualOwnership = tradingAssets.associateWith { asset -> CoinCalculation(asset, data.filter { it.hasAsset(asset) }.sumOf { it.getAmount(asset.coin1) }) }
        //traded amounts => values what I'd have had if without losts/gifts/etc => value used for price per unit
        val tradedAmount = tradingAssets.associateWith { asset ->
            CoinCalculation(asset, data.filterIsInstance<Transaction.Trade>().filter { it.hasAsset(asset) }.sumOf { it.getAmount(asset.coin1) })
        }
        val spentFiatByCrypto = tradingAssets.associateWith { asset ->
            CoinCalculation(CryptoCoin(asset.coin1), data.filter { it.hasAsset(asset) }.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(asset.coin2) })
        }
        val cryptoHoldings = tradingAssets.filter { it.hasCryptoCoin }.associateWith { asset ->
            CryptoHoldings(
                asset,
                actualOwnership.getValue(asset).value,
                tradedAmount.getValue(asset).value,
                spentFiatByCrypto.getValue(asset).value.abs(),
                feesPerCoin[asset.cryptoCoinOrNull()?.item] ?: ZERO
            )
        }

        val exchangeSumOfCoins = transactionsByExchange.mapNotNull { (exchange, transactions) ->
            ExchangeWallet(exchange) to allCoins
                .map { anyCoin -> CoinCalculation(AnyCoin(anyCoin), transactions.sumOf { it.getAmount(anyCoin) }) }
                .filter { it.value.isNotZero() }
        }.filter { it.second.isNotEmpty() }.toMap()

        val transactionsPerAssetPerType = transactionTypes.map { type -> type to data.filter { it.type == type } }
            .map { (type, transactions) -> Triple(type, transactions.map { it.asset }.toSet(), transactions) }.map { (type, assets, transactions) ->
                type to assets.map { asset ->
                    asset to transactions.filter { it.hasAsset(asset) }.let { ts -> ts.sumOf { it.getAmount(asset.coin2) } to ts.sumOf { it.getAmount(asset.coin1) } }
                }
            }

        val coinSumPerExchange = allCoins.associateWith { coin ->
            //!!Original items must be used!!!, otherwise converted values would screw the real amounts
            ledger.items.filter { it.asset.has(coin) }.groupBy { it.exchange }.mapValues { (_, transactions) -> transactions.sumOf { transaction -> transaction.getAmount(coin) } }
                .filter { it.value.isNotZero() }
                .map { (exchange, sum) -> CoinExchangeStats(AnyCoin(coin), ExchangeWallet(exchange.normalizedExchange()), sum, sum.safeDiv(sumOfCoins.getValue(coin))) }
        }

        return LedgerStats(tradingAssets.toList(), assetsByExchange, feesPerCoin, cryptoHoldings, coinSumPerExchange, exchangeSumOfCoins, transactionsPerAssetPerType)
    }


    private fun String.normalizedExchange(): String {
        return when {
            contains("kraken", ignoreCase = true) -> "Kraken"
            contains("binance", ignoreCase = true) -> "Binance"
            contains("trezor", ignoreCase = true) -> "Trezor"
            else -> this
        }
    }

    fun calculateMarketDailyGains(appData: AppData, primaryCurrency: String) = calculateMarketDailyGains(appData.ledger, appData.historyPrices, primaryCurrency)

    fun calculateMarketDailyGains(
        ledger: Ledger, prices: Map<Asset, List<PriceItem>>, primaryCurrency: String
    ): List<GroupStatsSum> {
        if (ledger.items.isEmpty()) return emptyList()

        require(prices.isNotEmpty()) { "Prices are empty" }
        require(FiatCurrencies.contains(primaryCurrency)) { "Invalid primaryCurrency:${primaryCurrency}, not defined as Fiat" }

        val latestCommonPriceDate = prices.minOf { (_, v) -> v.maxOf { it.dateTime } }
        val grouping = GroupStrategy.Day
        val data = ledger.items/*.filter { it.asset.has("SOL") && it.type != "Staking" }*/.sortedBy { it.dateTime }
        val groups = data.groupBy { grouping(it.dateTime) }
        val allPrices = prices.values.flatten()
        val pricesByAssetByGroup = allPrices.groupBy { grouping(it.dateTime) }.mapValues { it.value.associateBy { transactionsInGroup -> transactionsInGroup.asset } }

        val result = mutableMapOf<Long, MutableMap<Long, GroupStats>>()
        val startDate = grouping.previous(ledger.items.minOf { it.dateTime })
        val endDate = latestCommonPriceDate
        val groupKeys = mutableListOf<Long>()
        var date = startDate
        while (date <= endDate) {
            val key = grouping(date)
            groupKeys.add(key)
            date = grouping.next(date)
        }
        val groupKeysCombination = groupKeys.mapIndexed { index, targetGroupKey ->
            result[targetGroupKey] = TreeMap<Long, GroupStats>()
            (index downTo 0).map { i -> groupKeys[i] to targetGroupKey }
        }.flatten().sortedBy { it.first }

        groupKeysCombination.forEach { (targetGroupKey, priceGroupKey) ->
            val priceForGroup = pricesByAssetByGroup[priceGroupKey] ?: emptyMap()
            val (cost, marketPrice) = groups[targetGroupKey]?.totalMarketValue(priceForGroup, primaryCurrency) ?: MarketData.Empty
            result.getValue(targetGroupKey)[priceGroupKey] = GroupStats(targetGroupKey, cost, marketPrice)
        }

        val r = TreeMap<LocalDateTime, GroupStatsSum>()
        date = startDate
        while (date <= endDate) {
            val key = grouping(date)
            r[date] = result.sumForGroupKey(key, date)
            date = grouping.next(date)
        }

        var latestDayStatsSum = r.firstEntry().value
        //replace empty values by last with value
        r.forEach { (dateTime, stats) ->
            if (stats.isEmpty) {
                r[dateTime] = latestDayStatsSum.copy(groupingKey = grouping(dateTime))
            } else {
                latestDayStatsSum = stats
            }
        }
        return r.values.toList()
    }
}


private fun Map<Long, MutableMap<Long, GroupStats>>.sumForGroupKey(upperBoundGroupingKey: Long, date: LocalDateTime): GroupStatsSum {
    var sumCost = 0.bd
    var sumValue = 0.bd
    asSequence()
        .filter { (k, _) -> k <= upperBoundGroupingKey }
        .map { (k, v) -> k to (v[upperBoundGroupingKey] ?: GroupStats.empty(k)) }
        .forEach { (_, stats) ->
            sumCost += stats.cost
            sumValue += stats.value
        }
    return GroupStatsSum(upperBoundGroupingKey, date, sumCost, sumValue)
}

private data class GroupStats(
    val groupingKey: Long,
    val cost: BigDecimal,
    val value: BigDecimal
) {
    companion object {
        fun empty(groupingKey: Long) = GroupStats(groupingKey, 0.bd, 0.bd)
    }
}
