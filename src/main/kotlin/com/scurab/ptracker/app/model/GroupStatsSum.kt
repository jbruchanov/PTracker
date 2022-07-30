package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.colored
import com.scurab.ptracker.app.ext.f2
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.math.BigDecimal

data class GroupStatsSum(
    val groupingKey: Long,
    val localDateTime: LocalDateTime,
    val cost: BigDecimal,
    val marketPrice: BigDecimal
) {
    val isEmpty get() = cost.isZero() && marketPrice.isZero()

    val formattedDateTime by lazy { localDateTime.toJavaLocalDateTime().format(DateTimeFormats.dayFullDate) }

    val percents = marketPrice.safeDiv(cost).toFloat().let {
        val v = (100f * when {
            cost.isZero() -> 0f
            it > 1f -> it - 1f
            else -> -(1 - it)
        })
        when {
            v >= 0f -> "+${v.f2}%".colored(AppTheme.Colors.CandleGreen)
            else -> "${v.f2}%".colored(AppTheme.Colors.CandleRed)
        }
    }

    companion object {
        fun empty(groupingKey: Long, localDateTime: LocalDateTime) = GroupStatsSum(groupingKey, localDateTime, 0.bd, 0.bd)
    }
}
