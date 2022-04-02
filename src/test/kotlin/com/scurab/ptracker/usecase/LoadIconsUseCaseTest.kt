package com.scurab.ptracker.usecase

import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
import com.scurab.ptracker.serialisation.JsonBridge
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import test.TestCoins
import java.io.File

@Disabled
internal class LoadIconsUseCaseTest {

    @Test
    fun loadIcons() {
        val ledger = kotlin.runCatching { LoadLedgerUseCase().load(File("data/output2.xlsx")) }.getOrNull()
        val httpClient = defaultHttpClient()
        val assets = ledger?.assets ?: TestCoins
        val loadIconsUseCase = LoadIconsUseCase(httpClient, CryptoCompareClient(httpClient, mockk(), JsonBridge))
        runBlocking {
            loadIconsUseCase.loadIcons(assets)
        }
    }
}