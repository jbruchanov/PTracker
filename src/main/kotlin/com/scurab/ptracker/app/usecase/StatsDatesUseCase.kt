package com.scurab.ptracker.app.usecase

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.getAmount
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.app.ext.setOf
import com.scurab.ptracker.app.ext.toTableString
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.IDataTransformers
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.Texts
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
        val quantity1: BigDecimal,
        val grouping: DateGrouping,
        val count: Int,
        val quantity2: BigDecimal? = null
    ) {

        private val avgBuyPrice = quantity2?.abs()?.safeDiv(quantity1)?.abs()

        fun toTableItem(simple: Boolean): ITableItem {
            val values = if (simple) {
                listOf(grouping.format(groupDate), coin, quantity1.toTableString(6), count.toString())
            } else {
                listOf(grouping.format(groupDate), coin, quantity1.toTableString(6), quantity2?.toTableString(6) ?: "", avgBuyPrice?.toTableString(2) ?: "", count.toString())
            }
            return object : ITableItem {
                override fun getValue(index: Int, render: IDataTransformers): String = values[index]
            }
        }

        companion object {
            private val headers = listOf<Texts.() -> String>({ Date }, { Coin }, { Quantity }, { Count })
            private val headers2 = listOf<Texts.() -> String>({ Date }, { Asset }, { Coin + "1" }, { Coin + "2" }, { "${Coin}2/${Coin}1" }, { Count })
            fun tableMetaData(grouping: DateGrouping, asset: Asset?) = object : ITableMetaData {
                override val columns: Int = when {
                    asset?.isTradingAsset != true -> headers.size
                    else -> headers2.size
                }

                @Composable
                override fun getHeaderTitle(index: Int): String {
                    val labels = when {
                        asset?.isTradingAsset != true -> headers
                        else -> headers2
                    }
                    return labels[index](LocalTexts.current)
                }

                override fun getColumnWidth(index: Int, tableData: ITableData): TableCellSize = when (index) {
                    0 -> when (grouping) {
                        DateGrouping.NoGrouping -> TableCellSize.Exact(160.dp)
                        DateGrouping.Day -> TableCellSize.Exact(112.dp)
                        else -> TableCellSize.Exact(96.dp)
                    }

                    1 -> TableCellSize.Exact(96.dp)
                    2 -> TableCellSize.Exact(96.dp)
                    3 -> TableCellSize.Exact(128.dp)
                    else -> TableCellSize.Exact(96.dp)
                }
            }
        }
    }

    fun getStats(ledger: Ledger, grouping: DateGrouping, asset: Asset? = null): List<ITableItem> {
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
            .map {
                if (asset == null || it.size != 2) it
                else {
                    val coin1 = it.first { it.coin == asset.coin1 }
                    val coin2 = it.first { it.coin == asset.coin2 }
                    listOf(StatsItem(coin1.groupDate, asset.toString(), coin1.quantity1, coin1.grouping, coin1.count, coin2.quantity1))
                }
            }
            .flatten()
            .toList()
            .map { it.toTableItem(simple = asset?.isTradingAsset != true) }
    }

    private fun Asset.containsAnyOfTransactionCoin(transaction: Transaction) = when (transaction) {
        is Transaction.Outcome -> has(transaction.sellAsset)
        is Transaction.Income -> has(transaction.buyAsset)
        is Transaction.Trade -> contains(transaction.buyAsset, transaction.sellAsset)
        else -> throw IllegalStateException("Unhandled case for:$transaction")
    }
}

