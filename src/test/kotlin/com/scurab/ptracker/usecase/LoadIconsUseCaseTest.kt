package com.scurab.ptracker.usecase

import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
internal class LoadIconsUseCaseTest {

    @Test
    fun loadIcons() {
        val httpClient = defaultHttpClient()
        val coins = listOf("BTC", "ETH", "LTC", "ADA", "DOT", "AVAX", "AAVE")
        val loadIconsUseCase = LoadIconsUseCase(httpClient, CryptoCompareClient(httpClient))
        runBlocking {
            loadIconsUseCase.loadIcons(coins)
        }
    }
}