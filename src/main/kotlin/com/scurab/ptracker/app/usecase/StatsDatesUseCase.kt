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
import com.scurab.ptracker.ui.model.ITableData
import com.scurab.ptracker.ui.model.ITableItem
import com.scurab.ptracker.ui.model.ITableMetaData
import com.scurab.ptracker.ui.model.TableCellSize
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

class StatsDatesUseCase {

    data class StatsItem(
        val groupDate: LocalDate,
        val coin: String,
        val quantity: BigDecimal,
        val grouping: DateGrouping
    ) : ITableItem {

        override fun getValue(index: Int, render: IDataTransformers): String = when (index) {
            0 -> grouping.format(groupDate)
            1 -> coin
            2 -> quantity.toTableString(6)
            else -> throw IllegalArgumentException("Invalid column index:$index")
        }

        companion object : ITableMetaData {
            override val columns: Int = 3

            @Composable
            override fun getHeaderTitle(index: Int): String = when (index) {
                0 -> "Date"
                1 -> "Coin"
                2 -> "Quantity"
                else -> throw IllegalArgumentException("Invalid column index:$index")
            }

            override fun getColumnWidth(index: Int, tableData: ITableData): TableCellSize = when (index) {
                0 -> TableCellSize.Exact(80.dp)
                1 -> TableCellSize.Exact(48.dp)
                2 -> TableCellSize.Exact(128.dp)
                else -> throw IllegalArgumentException("Invalid column index:$index")
            }
        }
    }

    fun getStats(ledger: Ledger, grouping: DateGrouping, asset: Asset? = null): List<StatsItem> {
        return ledger.items
            .asSequence()
            .filterIsInstance<Transaction.Trade>()
            .filter { transaction ->
                asset == null ||
                        (asset.isTradingAsset && transaction.hasAsset(asset)) ||
                        (asset.isSingleCoinAsset && asset.contains(transaction.buyAsset, transaction.sellAsset))
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
                    .map { coin -> StatsItem(dateGroup, coin, transactions.filter { it.hasCoin(coin) }.sumOf { it.getAmount(coin) }, grouping) }
                    .sortedBy { it.coin }
            }
            .flatten()
            .toList()
    }
}

