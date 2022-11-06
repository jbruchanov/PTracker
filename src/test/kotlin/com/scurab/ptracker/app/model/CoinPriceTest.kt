package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import org.junit.jupiter.api.Test

internal class CoinPriceTest {

    @Test
    fun `convertCurrency WHEN same THEN nothing converted`() {
        val cp1 = CoinPrice(Asset("BTC", "GBP"), 30000.bd)
        val coinPrice = CoinPrice(Asset("GBP", "EUR"), 1.2.bd)

        val result = cp1.convertCurrency(coinPrice, "GBP")
        kotlin.test.assertEquals(result.asset, Asset("BTC", "GBP"))
        kotlin.test.assertEquals(result.price, 30000.bd)
    }

    @Test
    fun `flip WHEN same THEN nothing converted`() {
        val cp1 = CoinPrice(Asset("BTC", "GBP"), 30000.bd)
        val result = cp1.flipAsset()
        kotlin.test.assertEquals(result.asset, Asset("GBP", "BTC"))
        kotlin.test.assertEquals(result.price, 1.bd / 30000.bd)
    }

    @Test
    fun convertToAnotherCurrency1() {
        val cp1 = CoinPrice(Asset("BTC", "GBP"), 30000.bd)
        val coinPrice = CoinPrice(Asset("GBP", "EUR"), 1.2.bd)

        val result = cp1.convertCurrency(coinPrice, "EUR")
        kotlin.test.assertEquals(result.asset, Asset("BTC", "EUR"))
        kotlin.test.assertTrue(result.price.compareTo(36000.bd) == 0)
    }

    @Test
    fun convertToAnotherCurrency2() {
        val cp1 = CoinPrice(Asset("BTC", "GBP"), 30000.bd)
        val coinPrice = CoinPrice(Asset("EUR", "GBP"), 1.bd / 1.2.bd)

        val result = cp1.convertCurrency(coinPrice, "EUR")
        kotlin.test.assertEquals(result.asset, Asset("BTC", "EUR"))
        kotlin.test.assertTrue(result.price.compareTo(36000.bd) == 0)
    }
}
