package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HoldingsTest {

    @Test
    fun convertToAnotherCurrency() {
        val holdings = Holdings(
            asset = Asset("BTC", "EUR"), 0.1.bd, 0.1.bd, 10000.bd
        )

        val cp1 = CoinPrice(Asset("BTC", "GBP"), 30000.bd)
        val coinPrice = CoinPrice(Asset("GBP", "EUR"), 1.2.bd)

        val result = cp1.convertCurrency(coinPrice, "EUR")
        assertEquals(result.asset, Asset("BTC", "EUR"))
        assertEquals(result.price, 36000.bd)
    }
}