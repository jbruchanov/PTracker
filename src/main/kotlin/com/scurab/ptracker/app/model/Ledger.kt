package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.allCoins
import com.scurab.ptracker.app.ext.groupValue
import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.ext.tradingAssets

class Ledger(
    val items: List<Transaction>,
    val primaryFiatCoin: String? = null
) {
    private val cacheByGroupStrategy = mutableMapOf<DateGrouping, Map<Long, List<Transaction>>>()
    private val cacheByItemAndAsset = mutableMapOf<KeyAssetGroupingKey, List<Transaction>>()
    private val cacheByAssets = mutableMapOf<KeyAssetTrades, List<Transaction>>()
    val coins by lazy { items.setOf { it.assets }.flatten().distinct().sorted() }
    val fiatCoins by lazy { coins.filter { FiatCurrencies.contains(it) }.toSet() }
    val cryptoCoins by lazy { coins - fiatCoins }
    val assetsTradings by lazy { items.tradingAssets(primaryFiatCoin) }

    val assetsForPrices by lazy {
        val coins = items.setOf { it.asset }.distinct().allCoins()
        val (fiat, crypto) = coins.partition { FiatCurrencies.contains(it) }
        //in case of having like UST staking only
        val allCombinations = crypto.map { c -> fiat.map { f -> Asset(c, f) } }.flatten().toSet()
        //if there was some Fiat2Fiat exchange, ^ would remove it
        val allTradedAssets = items.setOf { it.asset }.filter { it.isTradingAsset }.toSet()
        (allCombinations + allTradedAssets)
    }

    fun getGroupedData(by: DateGrouping = DateGrouping.Day): Map<Long, List<Transaction>> {
        return cacheByGroupStrategy.getOrPut(by) {
            items.groupBy { by.toLongGroup(it.dateTime) }
        }
    }

    fun getTransactionsMap(priceItem: PriceItemUI, filter: Filter<Transaction>, grouping: DateGrouping = DateGrouping.Day): List<Transaction> {
        val key = KeyAssetGroupingKey(priceItem.asset, grouping.toLongGroup(priceItem.dateTime), filter)
        return cacheByItemAndAsset.getOrPut(key) {
            val groupedData = getGroupedData()
            groupedData[grouping.toLongGroup(priceItem.dateTime)]
                ?.asSequence()
                ?.filter(filter)
                ?.filter { it.hasOrIsRelatedAsset(priceItem.asset) }
                ?.toList()
                ?: emptyList()
        }
    }

    fun getTransactions(asset: Asset, filter: Filter<Transaction>): List<Transaction> {
        val key = KeyAssetTrades(asset, filter)
        return cacheByAssets.getOrPut(key) {
            items.asSequence()
                .filter(filter)
                .filter { it.hasOrIsRelatedAsset(asset) }
                .toList()
        }
    }

    fun firstIndexOf(it: PriceItemUI, grouping: DateGrouping = DateGrouping.Day): Int {
        val key = grouping.toLongGroup(it.dateTime)
        return items.indexOfFirst { grouping.toLongGroup(it.dateTime) == key }
    }

    fun fillPriceItems(priceItems: List<PriceItemUI>, groupStrategy: DateGrouping) {
        val groupKeyPriceItems = priceItems.associateBy { it.groupValue(groupStrategy) }
        require(groupKeyPriceItems.size == priceItems.size) {
            "Invalid priceItems vs groupStrategy:${groupStrategy.name}, " +
                "groupValue must generate unique values for each priceItem, " +
                "priceItems:${priceItems.size}, " +
                "groupedItems:${groupKeyPriceItems.size}"
        }
        items.forEach {
            it.priceItem = groupKeyPriceItems[it.groupValue(groupStrategy)]
        }
    }

    companion object {
        val Empty = Ledger(emptyList())
    }

    private data class KeyAssetGroupingKey(val asset: Asset, val groupingKey: Long, val filter: Filter<*>)
    private data class KeyAssetTrades(val asset: Asset, val filter: Filter<*>)
}
