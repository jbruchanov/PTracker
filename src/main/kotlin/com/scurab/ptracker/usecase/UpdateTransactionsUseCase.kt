package com.scurab.ptracker.usecase

import com.scurab.ptracker.ext.groupValue
import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.Transaction

class UpdateTransactionsUseCase {

    fun fillPriceItems(transactions: List<Transaction>, priceItems: List<PriceItem>, groupStrategy: GroupStrategy) {
        val groupKeyPriceItems = priceItems.associateBy { it.groupValue(groupStrategy) }
        require(groupKeyPriceItems.size == priceItems.size) {
            "Invalid priceItems vs groupStrategy:${groupStrategy.name}, groupValue must generate unique values for each priceItem, priceItems:${priceItems.size}, groupedItems:${groupKeyPriceItems.size}"
        }
        transactions.forEach {
            it.priceItem = groupKeyPriceItems[it.groupValue(groupStrategy)]
        }
    }
}