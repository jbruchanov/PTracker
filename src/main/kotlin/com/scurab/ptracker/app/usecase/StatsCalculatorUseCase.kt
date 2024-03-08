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
        ledger: Ledger,
        filter: Filter<Transaction>,
        prices: Map<Asset, MarketPrice> = emptyMap(),
        primaryCurrency: String? = appSettings.primaryCoin
    ): LedgerStats {
        //predata
        val realData = ledger.items
        val convertedData = realData.filter(filter).let { data ->
            if (primaryCurrency == null) {
                data
            } else {
                data.map { transaction ->
                    transaction.convertTradePrice(prices, primaryCurrency)
                }
            }
        }
        val allCoins = convertedData.map { it.assets }.flatten().toSet()
        val fiatCoins = allCoins.filter { FiatCurrencies.contains(it) }.toSet()
        val tradingAssets = convertedData.tradingAssets(primaryCurrency)
        val exchanges = convertedData.setOf { it.exchange }
        val sumOfCoins = allCoins.associateWith { coin ->
            //must be real items, for example BTC-ETH exchange would be lost
            realData.filter { it.asset.has(coin) }.map { it.getAmount(coin) }.sumOf { it }
        }
        val feesPerCoin = allCoins.associateWith { coin -> convertedData.sumOf { transaction -> transaction.getFees(coin) } }
        val assetsByExchange = exchanges.map { exchange -> exchange to exchange.normalizedExchange() }
            .associateBy(
                keySelector = { ExchangeWallet(it.second) },
                valueTransform = { (exchange, normalized) ->
                    realData.asSequence().filterIsInstance<Transaction.Trade>().filter { it.exchange == exchange }.filter { it.asset.isTradingAsset }.map { it.asset }.toSet()
                        .sorted()
                }
            )

        //currently, what I have on exchange/wallet
        //val actualOwnership = tradingAssets.associateWith { asset -> CoinCalculation(asset, data.filter { it.hasOrIsRelatedAsset(asset) }.sumOf { it.getAmount(asset.coin1) }) }
        //traded amounts => values what I'd have had if without losts/gifts/etc => value used for price per unit
        val tradedAmount = tradingAssets.associateWith { asset ->
            CoinCalculation(asset, convertedData.filterIsInstance<Transaction.Trade>().filter { it.hasOrIsRelatedAsset(asset) }.sumOf { it.getAmount(asset.coin1) })
        }
        val spentFiatByCrypto = tradingAssets.associateWith { asset ->
            CoinCalculation(
                CryptoCoin(asset.coin1),
                convertedData.filter { it.hasOrIsRelatedAsset(asset) }.filterIsInstance<Transaction.Trade>().sumOf { it.getAmount(asset.coin2) }
            )
        }
        val cryptoHoldings = tradingAssets.filter { it.hasCryptoCoin }.associateWith { asset ->
            CryptoHoldings(
                asset,
                sumOfCoins.getValue(asset.coin1),
                tradedAmount.getValue(asset).value,
                spentFiatByCrypto.getValue(asset).value.abs(),
                feesPerCoin[asset.cryptoCoinOrNull()?.item] ?: ZERO
            )
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

        return LedgerStats(tradingAssets.toList(), assetsByExchange, feesPerCoin, cryptoHoldings, coinSumPerExchange)
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
        require(FiatCurrencies.contains(primaryCurrency)) { "Invalid primaryCurrency:$primaryCurrency, not defined as Fiat" }
        require(dateGrouping != DateGrouping.NoGrouping) { "Invalid grouping:$dateGrouping" }

        val now = now()
        val latestCommonPriceDate = pricesGrouped.minOfOrNull { (_, v) -> v.maxOfOrNull { it.dateTime } ?: now }

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
                        val marketData = kotlin.runCatching {
                            transactionsPerGroup[historyGroup]?.totalMarketValue(pricesForStatsGroup, primaryCurrency, doSumCrypto)
                        }.onFailure { println(it.printStackTrace()) }.getOrNull() ?: MarketData.Empty
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
