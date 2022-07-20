package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.groupingYMD
import com.scurab.ptracker.app.ext.withDayOfWeek
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus

enum class DateGrouping(
    private val groupToLocalDateTime: (LocalDateTime) -> LocalDate,
    private val transformDateToLong: (LocalDateTime) -> Long
) {
    Day(groupToLocalDateTime = { LocalDate(it.year, it.monthNumber, it.dayOfMonth) }, transformDateToLong = { it.groupingYMD() }) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.date.minus(DatePeriod(days = 1)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.date.plus(DatePeriod(days = 1)).atTime(0, 0, 0)
    },
    Week(groupToLocalDateTime = { it.withDayOfWeek(DayOfWeek.MONDAY/*todo US*/).date }, transformDateToLong = { TODO() }) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.withDayOfWeek(java.time.DayOfWeek.MONDAY).date.minus(DatePeriod(days = 7)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.withDayOfWeek(java.time.DayOfWeek.MONDAY).date.plus(DatePeriod(days = 7)).atTime(0, 0, 0)
    },
    Month(groupToLocalDateTime = { LocalDate(it.year, it.monthNumber, 1) }, transformDateToLong = { (it.year * 1_00L) + (it.monthNumber) }) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.date.minus(DatePeriod(months = 1)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.date.plus(DatePeriod(months = 1)).atTime(0, 0, 0)
    },
    Year(groupToLocalDateTime = { LocalDate(it.year, 1, 1) }, transformDateToLong = { it.year.toLong() }) {
        override fun previous(value: LocalDateTime): LocalDateTime = LocalDateTime(value.year - 1, 1, 1, 0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = LocalDateTime(value.year + 1, 1, 1, 0, 0, 0)
    };

    fun toLongGroup(value: LocalDateTime) = transformDateToLong(value)

    fun toLocalDateGroup(value: LocalDateTime) = groupToLocalDateTime(value)

    abstract fun previous(value: LocalDateTime): LocalDateTime
    abstract fun next(value: LocalDateTime): LocalDateTime
}