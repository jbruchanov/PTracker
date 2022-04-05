import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.model.Transaction
import kotlinx.datetime.toKotlinLocalDateTime
import org.apache.poi.ss.usermodel.Row

interface BittyTaxParser {
    fun Row.shouldSkip() = getCell(TYPE) == null || getCell(TIMESTAMP) == null
    val Row.timestamp get() = getCell(TIMESTAMP).localDateTimeCellValue.toKotlinLocalDateTime()
    val Row.type: String get() = getCell(TYPE).stringCellValue
    val Row.buyQuantity get() = getCell(BUY_QUANTITY)?.numericCellValue?.toBigDecimal()
    val Row.buyAsset get() = getCell(BUY_ASSET).stringCellValue
    val Row.buyValueInFiat get() = getCell(BUY_QUANTITY_FIAT)?.numericCellValue?.toBigDecimal()
    val Row.sellQuantity get() = getCell(SELL_QUANTITY)?.toString()?.toBigDecimal()
    val Row.sellAsset get() = getCell(SELL_ASSET).stringCellValue
    val Row.sellValueInFiat get() = getCell(SELL_QUANTITY_FIAT)?.numericCellValue?.toBigDecimal()
    val Row.feeQuantity get() = getCell(FEE_QUANTITY)?.numericCellValue?.toBigDecimal() ?: ZERO
    val Row.feeAsset get() = getCell(FEE_ASSET).stringCellValue
    val Row.feeValueInFiat get() = getCell(FEE_QUANTITY_FIAT)?.numericCellValue?.toBigDecimal()
    val Row.wallet get() = getCell(WALLET).stringCellValue
    val Row.note get() = getCell(NOTE)?.stringCellValue

    fun Row.toTransaction(exchange: String): Transaction? {
        if (shouldSkip()) return null
        val buyQuantity = buyQuantity
        val sellQuantity = sellQuantity
        return when {
            buyQuantity != null && sellQuantity != null -> {
                Transaction.Trade(
                    exchange = exchange,
                    type = type,
                    dateTime = timestamp,
                    buyQuantity = buyQuantity,
                    buyAsset = buyAsset,
                    buyValueInFiat = buyValueInFiat,
                    sellQuantity = sellQuantity,
                    sellAsset = sellAsset,
                    sellValueInFiat = sellValueInFiat,
                    feeQuantity = feeQuantity,
                    feeAsset = feeAsset,
                    feeValueInFiat = feeValueInFiat,
                    wallet = wallet,
                    note = note,
                )
            }
            buyQuantity != null -> {
                Transaction.Income(
                    exchange = exchange,
                    type = type,
                    dateTime = timestamp,
                    buyQuantity = buyQuantity,
                    buyAsset = buyAsset,
                    buyValueInFiat = buyValueInFiat,
                    feeQuantity = feeQuantity,
                    feeAsset = feeAsset,
                    feeValueInFiat = feeValueInFiat,
                    wallet = wallet,
                    note = note,
                )
            }
            sellQuantity != null -> {
                Transaction.Outcome(
                    exchange = exchange,
                    type = type,
                    dateTime = timestamp,
                    sellQuantity = sellQuantity,
                    sellAsset = sellAsset,
                    sellValueInFiat = sellValueInFiat,
                    feeQuantity = feeQuantity,
                    feeAsset = feeAsset,
                    feeValueInFiat = feeValueInFiat,
                    wallet = wallet,
                    note = note,
                )
            }
            else -> throw IllegalStateException("We shouldn't ever be here, the when is exhaustive")
        }
    }

    companion object {
        private const val TYPE = 0
        private const val BUY_QUANTITY = 1
        private const val BUY_ASSET = 2
        private const val BUY_QUANTITY_FIAT = 3
        private const val SELL_QUANTITY = 4
        private const val SELL_ASSET = 5
        private const val SELL_QUANTITY_FIAT = 6
        private const val FEE_QUANTITY = 7
        private const val FEE_ASSET = 8
        private const val FEE_QUANTITY_FIAT = 9
        private const val WALLET = 10
        private const val TIMESTAMP = 11
        private const val NOTE = 12
    }
}