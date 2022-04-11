package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.isZero
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

data class GroupStatsSum(val groupingKey: Long, val localDateTime: LocalDateTime, val cost: BigDecimal, val marketPrice: BigDecimal) {
    val isEmpty get() = cost.isZero() && marketPrice.isZero()

    companion object {
        fun empty(groupingKey: Long, localDateTime: LocalDateTime) = GroupStatsSum(groupingKey, localDateTime, 0.bd, 0.bd)
    }
}
