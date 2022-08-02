package com.scurab.ptracker.app.usecase

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.getAmount
import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.ext.toTableString
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.IDataTransformers
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.model.ITableData
import com.scurab.ptracker.ui.model.ITableItem
import com.scurab.ptracker.ui.model.ITableMetaData
import com.scurab.ptracker.ui.model.TableCellSize
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

class StatsDatesUseCase {

    data class StatsItem(
        val groupDate: LocalDateTime,
        val coin: String,
        val quantity: BigDecimal,
        val grouping: DateGrouping,
        val count: Int
    ) : ITableItem {

        override fun getValue(index: Int, render: IDataTransformers): String = when (index) {
            0 -> grouping.format(groupDate)
            1 -> coin
            2 -> quantity.toTableString(6)
            3 -> count.toString()
            else -> throw IllegalArgumentException("Invalid column index:$index")
        }

        companion object {
            fun tableMetaData(grouping: DateGrouping) = object : ITableMetaData {
                override val columns: Int = when (grouping) {
                    DateGrouping.NoGrouping -> 3
                    else -> 4
                }

                @Composable
                override fun getHeaderTitle(index: Int): String = when (index) {
                    0 -> LocalTexts.current.Date
                    1 -> LocalTexts.current.Coin
                    2 -> LocalTexts.current.Quantity
                    3 -> LocalTexts.current.Count
                    else -> throw IllegalArgumentException("Invalid column index:$index")
                }

                override fun getColumnWidth(index: Int, tableData: ITableData): TableCellSize = when (index) {
                    0 -> when (grouping) {
                        DateGrouping.NoGrouping -> TableCellSize.Exact(160.dp)
                        else -> TableCellSize.Exact(96.dp)
                    }
                    1 -> TableCellSize.Exact(48.dp)
                    2 -> TableCellSize.Exact(128.dp)
                    3 -> TableCellSize.Exact(128.dp)
                    else -> throw IllegalArgumentException("Invalid column index:$index")
                }
            }
        }
    }

    fun getStats(ledger: Ledger, grouping: DateGrouping, asset: Asset? = null): List<StatsItem> {
        return ledger.items
            .asSequence()
            .run {
                if (asset?.isSingleCoinAsset != false) this
                else filterIsInstance<Transaction.Trade>()
            }
            .filter { transaction ->
                asset == null ||
                        (asset.isTradingAsset && transaction.hasAsset(asset)) ||
                        (asset.isSingleCoinAsset && asset.containsAnyOfTransactionCoin(transaction))
            }
            .groupBy { grouping.toLocalDateGroup(it.dateTime) }
            .map { (dateGroup, transactions) ->
                val coins = if (asset != null) {
                    asset.toList()
                } else {
                    val fiatCoins = transactions.setOf { it.asset.fiatCoinOrNull()?.item }.filterNotNull()
                    val cryptoCoins = transactions.setOf { it.asset.cryptoCoinOrNull()?.item }.filterNotNull()
                    fiatCoins + cryptoCoins
                }
                coins
                    .map { coin ->
                        val relatedTransactions = transactions.filter { it.hasCoin(coin) }
                        StatsItem(dateGroup, coin, relatedTransactions.sumOf { it.getAmount(coin) }, grouping, relatedTransactions.size)
                    }
                    .sortedBy { it.coin }
            }
            .flatten()
            .toList()
    }

    private fun Asset.containsAnyOfTransactionCoin(transaction: Transaction) = when (transaction) {
        is Transaction.Outcome -> has(transaction.sellAsset)
        is Transaction.Income -> has(transaction.buyAsset)
        is Transaction.Trade -> contains(transaction.buyAsset, transaction.sellAsset)
        else -> throw IllegalStateException("Unhandled case for:$transaction")
    }
}

