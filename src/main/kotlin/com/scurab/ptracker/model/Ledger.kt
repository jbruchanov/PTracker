package com.scurab.ptracker.model

class Ledger(
    val items: List<Transaction>,
    val grouping: Grouping
) {
    private val cacheByGrouping = mutableMapOf<Grouping, Map<Long, List<Transaction>>>()
    private val cacheByItemAndAsset = mutableMapOf<Key, List<Transaction>>()
    val assets = items.mapNotNull { (it as? Transaction.Trade)?.asset }.toSet().sortedBy { it.text }

    fun getGroupedData(by: Grouping = grouping): Map<Long, List<Transaction>> {
        return cacheByGrouping.getOrPut(by) {
            items.groupBy { by.groupingKey(it.time) }
        }
    }

    fun getData(priceItem: PriceItem): List<Transaction> {
        val key = Key(priceItem.asset, grouping.groupingKey(priceItem.date))
        return cacheByItemAndAsset.getOrPut(key) {
            val groupedData = getGroupedData()
            groupedData[grouping.groupingKey(priceItem.date)]
                ?.filter { it.hasAsset(priceItem.asset) }
                ?: emptyList()
        }
    }

    companion object {
        val Empty = Ledger(emptyList(), Grouping.Day)
    }

    private data class Key(val asset: Asset, val groupingKey: Long)
}