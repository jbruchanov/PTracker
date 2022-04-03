package test

import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.model.Transaction
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

private fun trade(
    buy: BigDecimal, buyAsset: String,
    sell: BigDecimal, sellAsset: String,
    fee: BigDecimal, feeAsset: String,
    localDateTime: LocalDateTime = now()
) = Transaction.Trade(
    exchange = "test",
    type = Transaction.TypeTrade,
    dateTime = localDateTime,
    buyQuantity = buy,
    buyAsset = buyAsset,
    buyValueInFiat = null,
    sellQuantity = sell,
    sellAsset = sellAsset,
    sellValueInFiat = null,
    feeQuantity = fee,
    feeAsset = feeAsset,
    feeValueInFiat = null,
    wallet = "test",
    note = ""
)

