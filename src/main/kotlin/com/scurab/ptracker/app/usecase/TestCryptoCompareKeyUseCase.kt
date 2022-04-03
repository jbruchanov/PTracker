package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.ui.model.Validity

class TestCryptoCompareKeyUseCase(
    private val cryptoCompareClient: CryptoCompareClient
) {

    suspend fun testKey(key: String): Validity {
        return cryptoCompareClient.testKey(key)
    }
}