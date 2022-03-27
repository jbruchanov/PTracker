package com.scurab.ptracker.model

class Ledger(
    val items: List<Transaction>,
    val grouping: GroupStrategy
) {
    private val cacheByGroupStrategy = mutableMapOf<GroupStrategy, Map<Long, List<Transaction>>>()
    private val cacheByItemAndAsset = mutableMapOf<KeyAssetGroupingKey, List<Transaction>>()
    private val cacheByAssets = mutableMapOf<KeyAssetTrades, List<Transaction>>()
    val assets = items.mapNotNull { (it as? Transaction.Trade)?.asset }.toSet().sortedBy { it.text }

    fun getGroupedData(by: GroupStrategy = grouping): Map<Long, List<Transaction>> {
        return cacheByGroupStrategy.getOrPut(by) {
            items.groupBy { by.groupingKey(it.dateTime) }
        }
    }

    fun getData(priceItem: PriceItem, onlyTrades: Boolean = true): List<Transaction> {
        val key = KeyAssetGroupingKey(priceItem.asset, grouping.groupingKey(priceItem.dateTime), onlyTrades)
        return cacheByItemAndAsset.getOrPut(key) {
            val groupedData = getGroupedData()
            groupedData[grouping.groupingKey(priceItem.dateTime)]
                ?.asSequence()
                ?.filter { !onlyTrades || it is Transaction.Trade }
                ?.filter { it.hasAsset(priceItem.asset) }
                ?.toList()
                ?: emptyList()
        }
    }

    fun getTransactions(asset: Asset, onlyTrades: Boolean = true): List<Transaction> {
        val key = KeyAssetTrades(asset, onlyTrades)
        return cacheByAssets.getOrPut(key) {
            items.asSequence()
                .filter { !onlyTrades || it is Transaction.Trade }
                .filter { it.hasAsset(asset) }
                .toList()
        }
    }

    fun firstIndexOf(it: PriceItem): Int {
        val key = grouping.groupingKey(it.dateTime)
        return items.indexOfFirst { grouping.groupingKey(it.dateTime) == key }
    }

    companion object {
        val Empty = Ledger(emptyList(), GroupStrategy.Day)
    }

    private data class KeyAssetGroupingKey(val asset: Asset, val groupingKey: Long, val onlyTrades: Boolean)
    private data class KeyAssetTrades(val asset: Asset, val onlyTrades: Boolean)
}