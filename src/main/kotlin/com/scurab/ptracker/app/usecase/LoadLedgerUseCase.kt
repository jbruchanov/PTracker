package com.scurab.ptracker.app.usecase

import BittyTaxParser
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.repository.AppSettings
import okhttp3.internal.closeQuietly
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File


class LoadLedgerUseCase(
    private val appSettings: AppSettings
) : BittyTaxParser {

    fun load(file: File): Ledger {
        val workbook: Workbook = XSSFWorkbook(file)
        val items = workbook.sheetIterator().asSequence()
            .toList()
            .map {
                val exchange = it.sheetName
                it.rowIterator().asSequence()
                    .drop(1)
                    .mapNotNull { it.toTransaction(exchange) }
                    .toList()
            }
            .flatten()
            .sortedByDescending { it.dateTime }
        workbook.closeQuietly()
        return Ledger(items, appSettings.primaryCoin)
    }
}