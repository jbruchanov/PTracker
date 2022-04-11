package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.groupingYMD
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

enum class GroupStrategy(val groupingKey: (LocalDateTime) -> Long) {
    Day({ it.groupingYMD() }) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.date.minus(DatePeriod(days = 1)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.date.plus(DatePeriod(days = 1)).atTime(0, 0, 0)
    };

    operator fun invoke(dateTime: LocalDateTime) = groupingKey.invoke(dateTime)

    abstract fun previous(value: LocalDateTime): LocalDateTime
    abstract fun next(value: LocalDateTime): LocalDateTime
}