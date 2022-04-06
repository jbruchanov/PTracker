package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.Transaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

internal class TransactionKtTest {
    @Test
    fun transactionConvertSell1() {
        val t1 = transaction(0.1.bd, "BTC", 4000.bd, "USD", 20.bd, "USD")
        val t2 = t1.convertTradePrice(CoinPrice(Asset("GBP", "USD"), 1.25.bd)) as Transaction.Trade

        assertEquals(t2.buyQuantity, 0.1.bd)
        assertEquals(t2.buyAsset, "BTC")
        assertEquals(t2.sellQuantity.align, 3200.bd.align)
        assertEquals(t2.sellAsset, "GBP")
        assertEquals(t2.feeQuantity.align, 16.bd.align)
        assertEquals(t2.feeAsset, "GBP")
    }

    @Test
    fun transactionConvertSell2() {
        val t1 = transaction(0.1.bd, "BTC", 4000.bd, "USD", 0.0001.bd, "BTC")
        val t2 = t1.convertTradePrice(CoinPrice(Asset("GBP", "USD"), 1.25.bd)) as Transaction.Trade

        assertEquals(t2.buyQuantity, 0.1.bd)
        assertEquals(t2.buyAsset, "BTC")
        assertEquals(t2.sellQuantity.align, 3200.bd.align)
        assertEquals(t2.sellAsset, "GBP")
        assertEquals(t2.feeQuantity.align, 0.0001.bd.align)
        assertEquals(t2.feeAsset, "BTC")
    }

    @Test
    fun transactionConvertBuy1() {
        val t1 = transaction(4000.bd, "USD", 0.1.bd, "BTC", 20.bd, "USD")
        val t2 = t1.convertTradePrice(CoinPrice(Asset("GBP", "USD"), 1.25.bd)) as Transaction.Trade

        assertEquals(t2.buyQuantity.align, 3200.bd.align)
        assertEquals(t2.buyAsset, "GBP")
        assertEquals(t2.sellQuantity, 0.1.bd)
        assertEquals(t2.sellAsset, "BTC")
        assertEquals(t2.feeQuantity.align, 16.bd.align)
        assertEquals(t2.feeAsset, "GBP")
    }

    @Test
    fun transactionConvertBuy2() {
        val t1 = transaction(4000.bd, "USD", 0.1.bd, "BTC", 0.0001.bd, "BTC")
        val t2 = t1.convertTradePrice(CoinPrice(Asset("GBP", "USD"), 1.25.bd)) as Transaction.Trade

        assertEquals(t2.buyQuantity.align, 3200.bd.align)
        assertEquals(t2.buyAsset, "GBP")
        assertEquals(t2.sellQuantity, 0.1.bd)
        assertEquals(t2.sellAsset, "BTC")
        assertEquals(t2.feeQuantity.align, 0.0001.bd.align)
        assertEquals(t2.feeAsset, "BTC")
    }

    private fun transaction(buyQuantity: BigDecimal, buyAsset: String, sellQuantity: BigDecimal, sellAsset: String, feeQuantity: BigDecimal, feeAsset: String) = Transaction.Trade(
        exchange = "Test",
        type = Transaction.TypeTrade,
        dateTime = now(),
        buyQuantity = buyQuantity,
        buyAsset = buyAsset,
        buyValueInFiat = null,
        sellQuantity = sellQuantity,
        sellAsset = sellAsset,
        sellValueInFiat = null,
        feeQuantity = feeQuantity,
        feeAsset = feeAsset,
        feeValueInFiat = null,
        wallet = "test",
        note = null
    )
}