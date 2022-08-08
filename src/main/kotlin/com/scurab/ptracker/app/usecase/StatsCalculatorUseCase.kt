package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.convertTradePrice
import com.scurab.ptracker.app.ext.getAmount
import com.scurab.ptracker.app.ext.getFees
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.ext.totalMarketValue
import com.scurab.ptracker.app.ext.tradingAssets
import com.scurab.ptracker.app.model.AnyCoin
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinCalculation
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.CryptoCoin
import com.scurab.ptracker.app.model.CryptoHoldings
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.FiatCurrencies
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.GroupStatsSum
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.LedgerStats
import com.scurab.ptracker.app.model.MarketData
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.PriceItemUI
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.repository.AppSettings
import kotlinx.datetime.LocalDateTime

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
            val src = if (FiatCurrencies.contains(coin)) ledger.items else data
            src.filter { it.asset.has(coin) }.map { it.getAmount(coin) }.sumOf { it }
        }
        val feesPerCoin = allCoins.associateWith { coin -> data.sumOf { transaction -> transaction.getFees(coin) } }
        val assetsByExchange = exchanges.map { exchange -> exchange to exchange.normalizedExchange() }
            .associateBy(
                keySelector = { ExchangeWallet(it.second) },
                valueTransform = { (exchange, normalized) ->
                    data.asSequence().filterIsInstance<Transaction.Trade>().filter { it.exchange == exchange }.filter { it.asset.isTradingAsset }.map { it.asset }.toSet().sorted()
                })

        //currently, what I have on exchange/wallet
        val actualOwnership = tradingAssets.associateWith { asset -> CoinCalculation(asset, data.filter { it.hasOrIsRelatedAsset(asset) }.sumOf { it.getAmount(asset.coin1) }) }
        //traded amounts => values what I'd have had if without losts/gifts/etc => value used for price per unit
        val tradedAmount = tradingAssets.associateWith { asset ->
            CoinCalculation(asset, data.filterIsInstance<Transaction.Trade>().filter { it.hasOrIsRelatedAsset(asset) }.sumOf { it.getAmount(asset.coin1) })
        }
        val spentFiatByCrypto = tradingAssets.associateWith { asset ->
            CoinCalculation(CryptoCoin(asset.coin1), data.filter { it.hasOrIsRelatedAsset(asset) }.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(asset.coin2) })
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
                    asset to transactions.filter { it.hasOrIsRelatedAsset(asset) }.let { ts -> ts.sumOf { it.getAmount(asset.coin2) } to ts.sumOf { it.getAmount(asset.coin1) } }
                }
            }

        val coinSumPerExchange = allCoins.associateWith { coin ->
            //!!Original items must be used!!!, otherwise converted values would screw the real amounts
            ledger.items
                .filter { it.asset.has(coin) }
                .groupBy { it.exchange }
                .mapValues { (_, transactions) -> transactions.sumOf { transaction -> transaction.getAmount(coin) } }
                .filter { it.value.isNotZero() }
                .map { (exchange, sum) ->
                    CoinExchangeStats(
                        coin = AnyCoin(coin),
                        exchange = ExchangeWallet(exchange.normalizedExchange()),
                        quantity = sum,
                        perc = sum.safeDiv(sumOfCoins.getValue(coin)),
                        price = primaryCurrency
                            ?.takeIf { coin != it }
                            ?.let { Asset(coin, it) }
                            ?.let { asset -> prices[asset] }
                            ?.let { it.price * sum }
                    )
                }
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

    fun calculateMarketDailyGains(
        transactions: List<Transaction>,
        pricesGrouped: Map<Asset, List<PriceItemUI>>,
        primaryCurrency: String,
        dateGrouping: DateGrouping,
        doSumCrypto: Boolean = false
    ): List<GroupStatsSum> {
        if (transactions.isEmpty()) return emptyList()
        require(pricesGrouped.isNotEmpty()) { "Prices are empty" }
        require(FiatCurrencies.contains(primaryCurrency)) { "Invalid primaryCurrency:${primaryCurrency}, not defined as Fiat" }
        require(dateGrouping != DateGrouping.NoGrouping) { "Invalid grouping:$dateGrouping" }

        val latestCommonPriceDate = pricesGrouped.minOfOrNull { (_, v) -> v.maxOf { it.dateTime } }

        val transactionsPerGroup = transactions
            .groupBy { dateGrouping.toLongGroup(it.dateTime) }

        val allPrices = pricesGrouped.values.flatten()
        val pricesByAssetByGroup = allPrices
            .groupBy { dateGrouping.toLongGroup(it.dateTime) }
            .mapValues { it.value.associateBy { transactionsInGroup -> transactionsInGroup.asset } }

        val startDate = dateGrouping.previous(transactions.minOf { it.dateTime })
        val endDate = latestCommonPriceDate ?: now()

        //groupKeys, aka value for each day we want to calculate stats for
        val groupKeys = mutableListOf<Pair<LocalDateTime, Long>>()
        var date = startDate
        while (date <= endDate) {
            val key = dateGrouping.toLongGroup(date)
            groupKeys.add(date to key)
            date = dateGrouping.next(date)
        }

        //region calculate values for each day using of statsDay prices
        //for each day
        val marketDataPerStatsGroup = groupKeys.indices
            .map { endIndex ->
                //day we calculate stats for
                val (statsGroupDate, statsGroup) = groupKeys[endIndex]
                val pricesForStatsGroup = pricesByAssetByGroup.getValue(statsGroup)
                //calculate from beginning value per each day
                val marketDataForStatsGroup = (0..endIndex)
                    .map { startIndex ->
                        //day of transactions in day before statsGroup
                        val historyGroup = groupKeys[startIndex].second
                        val marketData = transactionsPerGroup[historyGroup]
                            ?.totalMarketValue(pricesForStatsGroup, primaryCurrency, doSumCrypto)
                            ?: MarketData.Empty
                        marketData
                    }
                statsGroupDate to marketDataForStatsGroup
            }

        var latestGroupStatsSum: GroupStatsSum? = null
        val result = marketDataPerStatsGroup.map { (date, marketDataForStatsGroup) ->
            val key = dateGrouping.toLongGroup(date)
            val statsPerGroup = marketDataForStatsGroup.getGroupStatsSums(key, date)
            //replace empty values by last with value
            val latest = latestGroupStatsSum
            if (latest != null && !latest.isEmpty && statsPerGroup.isEmpty) {
                latest.copy(groupingKey = dateGrouping.toLongGroup(date))
            } else {
                latestGroupStatsSum = statsPerGroup
                statsPerGroup
            }
        }
        return result
    }
}

private fun List<MarketData>.getGroupStatsSums(
    upperBoundGroupingKey: Long,
    date: LocalDateTime
): GroupStatsSum {
    var sumCost = 0.bd
    var sumCrypto = 0.bd
    var sumMarketValue = 0.bd
    forEach { data ->
        sumCost += data.cost
        sumCrypto += data.sumCrypto
        sumMarketValue += data.marketValue
    }
    return GroupStatsSum(upperBoundGroupingKey, date, sumCost, sumCrypto, sumMarketValue)
}
