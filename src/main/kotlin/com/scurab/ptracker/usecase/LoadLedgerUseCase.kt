package com.scurab.ptracker.usecase

import BittyTaxParser
import com.scurab.ptracker.model.Grouping
import com.scurab.ptracker.model.Ledger
import okhttp3.internal.closeQuietly
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File


class LoadLedgerUseCase : BittyTaxParser {

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
        workbook.closeQuietly()
        return Ledger(items, Grouping.Day)
    }
}