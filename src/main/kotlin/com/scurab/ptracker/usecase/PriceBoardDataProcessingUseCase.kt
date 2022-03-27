package com.scurab.ptracker.usecase

import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.Filter
import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.Transaction

class PriceBoardDataProcessingUseCase {

    class RawData(
        val ledger: Ledger,
        val asset: Asset,
        val prices: List<PriceItem>
    )

    class Result(
        val assets: List<Asset>,
        val transactions: List<Transaction>,
        val transactionsPerPriceItem: Map<PriceItem, List<Transaction>>
    )

    fun prepareData(data: RawData, filter: Filter<Transaction>, grouping: GroupStrategy): Result = with(data) {
        data.ledger.fillPriceItems(data.prices, grouping)
        return Result(
            ledger.assets,
            ledger.getTransactions(asset, filter),
            prices.associateBy(keySelector = { it }, valueTransform = { ledger.getTransactionsMap(it, filter) })
        )
    }
}