package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.repository.AppSettingsJsonRepository
import com.scurab.ptracker.app.repository.MemoryAppSettings
import com.scurab.ptracker.app.serialisation.JsonBridge
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

//@Disabled
internal class StatsCalculatorUseCaseTest {

    @Test
    fun man() {
        val settings = MemoryAppSettings().apply { primaryCoin = "GBP" }
        val ledger = kotlin.runCatching { LoadLedgerUseCase(settings).load(File("data/output2.xlsx")) }.getOrThrow()
        StatsCalculatorUseCase(MemoryAppSettings()).calculateStats(
            ledger, Filter.AllTransactions
        )
    }
}