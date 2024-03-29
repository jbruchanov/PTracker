package com.scurab.ptracker.app.usecase

import androidx.compose.ui.res.loadImageBitmap
import com.scurab.ptracker.app.ext.iconColor
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.PriceItemUI
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.ui.model.AssetIcon
import kotlinx.datetime.LocalDateTime

class PriceBoardDataProcessingUseCase {

    class RawData(
        val ledger: Ledger,
        val asset: Asset,
        val prices: List<PriceItemUI>
    )

    class Result(
        val assets: List<Asset>,
        val assetsIcons: List<AssetIcon>,
        val transactions: List<Transaction>,
        val transactionsPerDateTime: Map<LocalDateTime, PriceItemTransactions>
    )

    fun prepareData(data: RawData, filter: Filter<Transaction>, grouping: DateGrouping): Result = with(data) {
        data.ledger.fillPriceItems(data.prices, grouping)
        return Result(
            ledger.assetsTradings,
            ledger.assetsTradings.map { AssetIcon(it, kotlin.runCatching { loadImageBitmap(it.iconCoin1().inputStream()) }.getOrNull()) },
            ledger.getTransactions(asset, filter),
            prices.associateBy(keySelector = { it.item.dateTime }, valueTransform = { PriceItemTransactions(it, ledger.getTransactionsMap(it, filter)) })
        )
    }
}

class PriceItemTransactions(
    val priceItem: PriceItemUI,
    val transactions: List<Transaction>
) {
    val iconPrices = transactions.map { it.iconColor() to it }

    val distinctIcons = transactions
        .map { it.iconColor() }
        .distinct()
        .sortedBy { it.priority }
}
