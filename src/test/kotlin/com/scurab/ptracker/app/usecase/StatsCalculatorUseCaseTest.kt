package com.scurab.ptracker.app.usecase

import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.repository.AppSettingsJsonRepository
import com.scurab.ptracker.app.serialisation.JsonBridge
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
internal class StatsCalculatorUseCaseTest {

    @Test
    fun man() {
        val ledger = kotlin.runCatching { LoadLedgerUseCase().load(File("data/output.xlsx")) }.getOrThrow()
        StatsCalculatorUseCase(AppSettingsJsonRepository.default(JsonBridge)).calculateStats(
            ledger, Filter.AllTransactions
        )
    }
}