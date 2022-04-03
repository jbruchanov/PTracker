package com.scurab.ptracker.app.usecase

import androidx.compose.ui.res.loadImageBitmap
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.GroupStrategy
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.ui.model.AssetIcon

class PriceBoardDataProcessingUseCase {

    class RawData(
        val ledger: Ledger,
        val asset: Asset,
        val prices: List<PriceItem>
    )

    class Result(
        val assets: List<Asset>,
        val assetsIcons: List<AssetIcon>,
        val transactions: List<Transaction>,
        val transactionsPerPriceItem: Map<PriceItem, List<Transaction>>
    )

    fun prepareData(data: RawData, filter: Filter<Transaction>, grouping: GroupStrategy): Result = with(data) {
        data.ledger.fillPriceItems(data.prices, grouping)
        return Result(
            ledger.assets,
            ledger.assets.map { AssetIcon(it, kotlin.runCatching { loadImageBitmap(it.iconCrypto().inputStream()) }.getOrNull()) },
            ledger.getTransactions(asset, filter),
            prices.associateBy(keySelector = { it }, valueTransform = { ledger.getTransactionsMap(it, filter) })
        )
    }
}