package com.scurab.ptracker.app.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.jibru.kostra.KQualifiers
import com.scurab.ptracker.K
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.colored
import com.scurab.ptracker.app.ext.f2
import com.scurab.ptracker.app.ext.hrs
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.compose.get
import com.scurab.ptracker.get
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.math.BigDecimal

data class GroupStatsSum(
    val groupingKey: Long,
    val localDateTime: LocalDateTime,
    val cost: BigDecimal,
    //non 0 only if the data was filtered for 1 transaction
    val sumCrypto: BigDecimal,
    val marketValue: BigDecimal
) {
    val isEmpty get() = cost.isZero() && marketValue.isZero()

    val formattedDateTime by lazy { localDateTime.toJavaLocalDateTime().format(DateTimeFormats.dayFullDate) }

    val maxOfCostOrPrice = cost.max(marketValue)
    val minOfCostOrPrice = cost.min(marketValue)

    val avgCryptoPrice = cost.safeDiv(sumCrypto)

    val percents = marketValue.safeDiv(cost).toFloat().let {
        val v = (
            100f * when {
                cost.isZero() -> 0f
                it > 1f -> it - 1f
                else -> -(1 - it)
            }
            )
        when {
            v >= 0f -> "+${v.f2}%".colored(AppTheme.Colors.CandleGreen)
            else -> "${v.f2}%".colored(AppTheme.Colors.CandleRed)
        }
    }

    fun detail(qualifiers: KQualifiers) = buildAnnotatedString {
        append(AnnotatedString("${K.string.MarketValue.get(qualifiers)}: ${marketValue.hrs()}", spanStyle = SpanStyle(color = AppTheme.Colors.CandleGreen)))
        append(" ")
        append(AnnotatedString("${K.string.Cost.get(qualifiers)}: -${cost.abs().hrs()}", spanStyle = SpanStyle(color = AppTheme.Colors.CandleRed)))
        append(" ")
        append(percents)
        if (avgCryptoPrice.isNotZero()) {
            append(" ")
            append(
                AnnotatedString(
                    "${K.string.AvgPricePerCoin.get(qualifiers)}: ${avgCryptoPrice.hrs()}",
                    spanStyle = SpanStyle(color = AppTheme.Colors.Secondary)
                )
            )
        }
    }

    companion object {
        fun empty(groupingKey: Long, localDateTime: LocalDateTime) = GroupStatsSum(groupingKey, localDateTime, 0.bd, 0.bd, 0.bd)
    }
}
