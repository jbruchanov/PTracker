package com.scurab.ptracker.app.usecase

import BittyTaxParser
import com.scurab.ptracker.app.ext.existsAndHasSize
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.toLocalDateTime
import com.scurab.ptracker.app.model.Ledger
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.component.util.Hashing
import kotlinx.datetime.daysUntil
import okhttp3.internal.closeQuietly
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.net.URI
import kotlin.math.absoluteValue


class LoadLedgerUseCase(
    private val appSettings: AppSettings,
    private val mapping: (Transaction) -> Transaction?
) : BittyTaxParser {

    fun load(uri: String): Ledger {
        val workbook: Workbook = XSSFWorkbook(fileOrDownload(uri))
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
            .mapNotNull(mapping)
        workbook.closeQuietly()
        return Ledger(items, appSettings.primaryCoin)
    }

    private fun fileOrDownload(location: String): File {
        val uri = kotlin.runCatching { URI.create(location) }.getOrNull() ?: return File(location)
        val targetDir = File(Locations.TempData).also { it.mkdirs() }
        val target = File(targetDir, Hashing.md5(location))
        val lastModified = target.lastModified().toLocalDateTime()
        if (!target.existsAndHasSize() || lastModified.date.daysUntil(now().date).absoluteValue != 0) {
            target.delete()
            target.writeBytes(uri.toURL().openStream().readAllBytes())
        }
        return target
    }
}

