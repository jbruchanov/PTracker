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
            .map { sheet ->
                val exchange = sheet.sheetName
                sheet.rowIterator().asSequence()
                    .drop(1)
                    .mapIndexedNotNull { index, row ->
                        try {
                            row.toTransaction(index, exchange)
                        } catch (e: Exception) {
                            throw IllegalStateException("Unable to read row:${index + 1} sheet:${sheet.sheetName}", e)
                        }
                    }
                    .toList()
            }
            .flatten()
            .sortedByDescending { it.dateTime }
        workbook.closeQuietly()
        return Ledger(items, appSettings.primaryCoin)
    }
}