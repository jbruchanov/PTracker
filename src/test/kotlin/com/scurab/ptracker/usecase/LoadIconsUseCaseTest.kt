package com.scurab.ptracker.usecase

import com.scurab.ptracker.app.repository.AppSettingsJsonRepository
import com.scurab.ptracker.app.repository.MemoryAppSettings
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.app.usecase.LoadIconsUseCase
import com.scurab.ptracker.app.usecase.LoadLedgerUseCase
import com.scurab.ptracker.app.util.LedgerParsingProcessor
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
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
        val ledger = kotlin.runCatching { LoadLedgerUseCase(MemoryAppSettings(), LedgerParsingProcessor()).load(File("data/output.xlsx")) }.getOrNull()
        val httpClient = defaultHttpClient()
        val assets = ledger?.assetsTradings ?: TestCoins
        val loadIconsUseCase = LoadIconsUseCase(AppSettingsJsonRepository.default(JsonBridge), httpClient, CryptoCompareClient(httpClient, mockk(), JsonBridge))
        runBlocking {
            loadIconsUseCase.loadIcons(assets)
        }
    }
}
