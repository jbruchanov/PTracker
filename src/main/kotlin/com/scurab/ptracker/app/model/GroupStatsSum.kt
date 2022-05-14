package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.math.BigDecimal

data class GroupStatsSum(val groupingKey: Long, val localDateTime: LocalDateTime, val cost: BigDecimal, val marketPrice: BigDecimal) {
    val isEmpty get() = cost.isZero() && marketPrice.isZero()

    val formattedDateTime by lazy { localDateTime.toJavaLocalDateTime().format(DateTimeFormats.fullDate) }

    companion object {
        fun empty(groupingKey: Long, localDateTime: LocalDateTime) = GroupStatsSum(groupingKey, localDateTime, 0.bd, 0.bd)
    }
}
