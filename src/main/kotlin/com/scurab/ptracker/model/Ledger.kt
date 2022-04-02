package com.scurab.ptracker.model

import com.scurab.ptracker.ext.groupValue

class Ledger(
    val items: List<Transaction>,
    val grouping: GroupStrategy
) {
    private val cacheByGroupStrategy = mutableMapOf<GroupStrategy, Map<Long, List<Transaction>>>()
    private val cacheByItemAndAsset = mutableMapOf<KeyAssetGroupingKey, List<Transaction>>()
    private val cacheByAssets = mutableMapOf<KeyAssetTrades, List<Transaction>>()
    val coins by lazy { items.map { it.assets }.toSet().flatten().distinct().sorted() }
    val fiatCoins by lazy { coins.filter { FiatCurrencies.contains(it) }.toSet() }
    val cryptoCoins by lazy { coins - fiatCoins }
    val assets by lazy { items.map { it.asset }.filter { it.isCryptoTradingAsset }.toSet().sorted() }

    fun getGroupedData(by: GroupStrategy = grouping): Map<Long, List<Transaction>> {
        return cacheByGroupStrategy.getOrPut(by) {
            items.groupBy { by.groupingKey(it.dateTime) }
        }
    }

    fun getTransactionsMap(priceItem: PriceItem, filter: Filter<Transaction>): List<Transaction> {
        val key = KeyAssetGroupingKey(priceItem.asset, grouping.groupingKey(priceItem.dateTime), filter)
        return cacheByItemAndAsset.getOrPut(key) {
            val groupedData = getGroupedData()
            groupedData[grouping.groupingKey(priceItem.dateTime)]
                ?.asSequence()
                ?.filter(filter)
                ?.filter { it.hasAsset(priceItem.asset) }
                ?.toList()
                ?: emptyList()
        }
    }

    fun getTransactions(asset: Asset, filter: Filter<Transaction>): List<Transaction> {
        val key = KeyAssetTrades(asset, filter)
        return cacheByAssets.getOrPut(key) {
            items.asSequence()
                .filter(filter)
                .filter { it.hasAsset(asset) }
                .toList()
        }
    }

    fun firstIndexOf(it: PriceItem): Int {
        val key = grouping.groupingKey(it.dateTime)
        return items.indexOfFirst { grouping.groupingKey(it.dateTime) == key }
    }

    fun fillPriceItems(priceItems: List<PriceItem>, groupStrategy: GroupStrategy) {
        val groupKeyPriceItems = priceItems.associateBy { it.groupValue(groupStrategy) }
        require(groupKeyPriceItems.size == priceItems.size) {
            "Invalid priceItems vs groupStrategy:${groupStrategy.name}, groupValue must generate unique values for each priceItem, priceItems:${priceItems.size}, groupedItems:${groupKeyPriceItems.size}"
        }
        items.forEach {
            it.priceItem = groupKeyPriceItems[it.groupValue(groupStrategy)]
        }
    }

    companion object {
        val Empty = Ledger(emptyList(), GroupStrategy.Day)
    }

    private data class KeyAssetGroupingKey(val asset: Asset, val groupingKey: Long, val filter: Filter<*>)
    private data class KeyAssetTrades(val asset: Asset, val filter: Filter<*>)
}