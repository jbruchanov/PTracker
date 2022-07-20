package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.Transaction
import org.junit.jupiter.api.Test
import test.trade
import java.math.BigDecimal
import kotlin.test.assertEquals

internal class TransactionKtTest {

    private fun coinPrice(coin1: String, coin2: String, price: BigDecimal) = listOf(CoinPrice(Asset(coin1, coin2), price)).associateBy { it.asset }

    @Test
    fun transactionConvertSell1() {
        val t1 = transaction(0.1.bd, "BTC", 4000.bd, "USD", 20.bd, "USD")
        val t2 = t1.convertTradePrice(coinPrice("GBP", "USD", 1.25.bd), "GBP") as Transaction.Trade

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
        val t2 = t1.convertTradePrice(coinPrice("GBP", "USD", 1.25.bd), "GBP") as Transaction.Trade

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
        val t2 = t1.convertTradePrice(coinPrice("GBP", "USD", 1.25.bd), "GBP") as Transaction.Trade

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
        val t2 = t1.convertTradePrice(coinPrice("GBP", "USD", 1.25.bd), "GBP") as Transaction.Trade

        assertEquals(t2.buyQuantity.align, 3200.bd.align)
        assertEquals(t2.buyAsset, "GBP")
        assertEquals(t2.sellQuantity, 0.1.bd)
        assertEquals(t2.sellAsset, "BTC")
        assertEquals(t2.feeQuantity.align, 0.0001.bd.align)
        assertEquals(t2.feeAsset, "BTC")
    }

    @Test
    fun testConversion() {
        val prices = listOf(
            CoinPrice(Asset("GBP", "EUR"), 1.25.bd),
            CoinPrice(Asset("BTC", "EUR"), 40000.bd),
            CoinPrice(Asset("BTC", "GBP"), 30000.bd),
        ).associateBy { it.asset }
        val ledger = Ledger(
            listOf(
                trade(0.1.bd, "BTC", 4000.bd, "EUR", 10.bd, "EUR"),
                trade(0.1.bd, "GBP", 3000.bd, "GBP", 10.bd, "GBP"),
            )
        )
        val txs = ledger.items.map { it.convertTradePrice(prices, "GBP") }
        val expected = (ledger.items[0] as Transaction.Trade).copy(
            sellQuantity = 4000.bd / 1.25.bd,
            sellAsset = "GBP",
            feeQuantity = 10.bd / 1.25.bd,
            feeAsset = "GBP"
        )
        assertEquals(expected, txs[0])
        assertEquals(ledger.items[1], txs[1])
    }

    private fun transaction(buyQuantity: BigDecimal, buyAsset: String, sellQuantity: BigDecimal, sellAsset: String, feeQuantity: BigDecimal, feeAsset: String) = Transaction.Trade(
        id = 0,
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