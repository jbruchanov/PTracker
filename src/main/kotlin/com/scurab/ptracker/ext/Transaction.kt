package com.scurab.ptracker.ext

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.scurab.ptracker.model.HasIncome
import com.scurab.ptracker.model.HasOutcome
import com.scurab.ptracker.model.Transaction
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.model.TransactionTextPrices
import kotlin.math.max

fun Transaction.iconColor() = when {
    type == "Deposit" -> AppTheme.TransactionIcons.Square
    type == "Lost" -> AppTheme.TransactionIcons.Cross
    this is Transaction.Income -> AppTheme.TransactionIcons.TriangleDown
    this is Transaction.Outcome -> AppTheme.TransactionIcons.TriangleUp
    this is Transaction.Trade -> AppTheme.TransactionIcons.Rhombus
    else -> AppTheme.TransactionIcons.Else
}

private fun String.annotatedPriceRow(prefix: String? = null, suffix: String? = null) = buildAnnotatedString {
    if (prefix != null) {
        append(prefix)
        append(": ")
    }
    append(this@annotatedPriceRow)
    if (suffix != null) {
        append(" ")
        append(
            AnnotatedString(
                text = suffix,
                spanStyle = SpanStyle(color = AppTheme.Colors.PrimaryVariant, fontSize = 10.sp)
            )
        )
    }
}

fun Transaction.formattedPrices(): TransactionTextPrices {
    val ba = (this as? HasIncome)?.buyAsset
    val bq = (this as? HasIncome)?.buyQuantity?.round(ba)
    val sa = (this as? HasOutcome)?.sellAsset
    val sq = (this as? HasOutcome)?.sellQuantity?.round(sa)
    val fq = feeQuantity.round(feeAsset).takeIf { !it.isZero() }

    val bn = bq?.base()?.coerceAtLeast(1) ?: 0
    val sn = sq?.base()?.coerceAtLeast(1) ?: 0
    val fn = fq?.base()?.coerceAtLeast(1) ?: 0

    val max = max(max(bn, sn), fn)
    val buy = bq?.let { v -> "+" + v.toPlainString().let { " ".repeat(max(0, max - bn)) + it } }
    val sell = sq?.let { v -> "-" + v.toPlainString().let { " ".repeat(max(0, max - sn)) + it } }
    val fee = fq?.let { v -> "-" + v.toPlainString().let { " ".repeat(max(0, max - fn)) + it } }

    return TransactionTextPrices(
        buy = buy?.annotatedPriceRow(ba),
        sell = sell?.annotatedPriceRow(sa),
        fee = fee?.annotatedPriceRow(feeAsset),
    )
}