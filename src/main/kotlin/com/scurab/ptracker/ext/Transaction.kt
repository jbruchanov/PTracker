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

fun Transaction.iconColor() = getOrPut("TransactionIcon") {
    when {
        this is Transaction.Trade && this.isCryptoBuy() -> AppTheme.TransactionIcons.IconsMap.getValue(Transaction._TypeTradeIn)
        this is Transaction.Trade && !this.isCryptoBuy() -> AppTheme.TransactionIcons.IconsMap.getValue(Transaction._TypeTradeOut)
        else -> AppTheme.TransactionIcons.IconsMap.getValue(type)
    }
}

private fun String.annotatedPrice(prefix: String? = null, suffix: String? = null) = buildAnnotatedString {
    if (prefix != null) {
        append(prefix)
        append(" ")
    }
    append(this@annotatedPrice)
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
    return getOrPut("formattedPrices") {
        val ba = (this as? HasIncome)?.buyAsset
        val bq = (this as? HasIncome)?.buyQuantity?.round(ba)
        val bl = ba?.length ?: 0
        val sa = (this as? HasOutcome)?.sellAsset
        val sq = (this as? HasOutcome)?.sellQuantity?.round(sa)
        val sl = sa?.length ?: 0
        val fq = feeQuantity.round(feeAsset).takeIf { !it.isZero() }
        val fa = feeAsset
        val fl = feeAsset.length
        val ua = " ".repeat(3)

        val bn = bq?.base()?.coerceAtLeast(1) ?: 0
        val sn = sq?.base()?.coerceAtLeast(1) ?: 0
        val fn = fq?.base()?.coerceAtLeast(1) ?: 0
        val up = this.unitPrice()
        //minus to shrink little but, unitPrice doesn't have currency symbol
        val un = up?.base()?.coerceAtLeast(1) ?: 0

        val maxn = max(max(max(bn, sn), fn), un)
        val maxa = max(max(bl, sl), fl)
        val buy = bq?.toPlainString()?.let { " ".repeat(max(0, maxn - bn)) + "+" + it }
        val sell = sq?.toPlainString()?.let { " ".repeat(max(0, maxn - sn)) + "-" + it }
        val fee = fq?.toPlainString()?.let { " ".repeat(max(0, maxn - fn)) + "-" + it }
        val price = up?.toPlainString()?.let { " ".repeat(max(0, maxn - un )) + " " + it }

        TransactionTextPrices(
            buy = buy?.annotatedPrice(ba),
            sell = sell?.annotatedPrice(sa),
            fee = fee?.annotatedPrice(feeAsset),
            unitPrice = price?.annotatedPrice(ua)
        )
    }
}