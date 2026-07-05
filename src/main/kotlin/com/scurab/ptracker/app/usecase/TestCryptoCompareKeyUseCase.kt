package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.net.MarketDataClient
import com.scurab.ptracker.ui.model.Validity

class TestCryptoCompareKeyUseCase(
    private val marketDataClient: MarketDataClient
) {

    suspend fun testKey(key: String): Validity {
        return marketDataClient.testKey(key)
    }
}
