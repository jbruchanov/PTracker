package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.groupingYMD
import com.scurab.ptracker.app.ext.withDayOfWeek
import com.scurab.ptracker.ui.DateTimeFormats
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

enum class DateGrouping(
    private val dateTimeFormatter: DateTimeFormatter,
    private val groupToLocalDateTime: (LocalDateTime) -> LocalDate,
    private val transformDateToLong: (LocalDateTime) -> Long
) {
    Day(
        dateTimeFormatter = DateTimeFormats.fullDate,
        groupToLocalDateTime = { LocalDate(it.year, it.monthNumber, it.dayOfMonth) },
        transformDateToLong = { it.groupingYMD() }
    ) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.date.minus(DatePeriod(days = 1)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.date.plus(DatePeriod(days = 1)).atTime(0, 0, 0)
    },
    Week(
        dateTimeFormatter = DateTimeFormats.yearWeek,
        groupToLocalDateTime = { it.withDayOfWeek(DayOfWeek.MONDAY/*todo US*/).date },
        transformDateToLong = { TODO() }
    ) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.withDayOfWeek(java.time.DayOfWeek.MONDAY).date.minus(DatePeriod(days = 7)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.withDayOfWeek(java.time.DayOfWeek.MONDAY).date.plus(DatePeriod(days = 7)).atTime(0, 0, 0)
    },
    Month(
        dateTimeFormatter = DateTimeFormats.yearMonthMid,
        groupToLocalDateTime = { LocalDate(it.year, it.monthNumber, 1) },
        transformDateToLong = { (it.year * 1_00L) + (it.monthNumber) }
    ) {
        override fun previous(value: LocalDateTime): LocalDateTime = value.date.minus(DatePeriod(months = 1)).atTime(0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = value.date.plus(DatePeriod(months = 1)).atTime(0, 0, 0)
    },
    Year(
        dateTimeFormatter = DateTimeFormats.year,
        groupToLocalDateTime = { LocalDate(it.year, 1, 1) },
        transformDateToLong = { it.year.toLong() }
    ) {
        override fun previous(value: LocalDateTime): LocalDateTime = LocalDateTime(value.year - 1, 1, 1, 0, 0, 0)
        override fun next(value: LocalDateTime): LocalDateTime = LocalDateTime(value.year + 1, 1, 1, 0, 0, 0)
    };

    fun toLongGroup(value: LocalDateTime) = transformDateToLong(value)

    fun toLocalDateGroup(value: LocalDateTime) = groupToLocalDateTime(value)

    fun format(value: LocalDate) = dateTimeFormatter.format(value.toJavaLocalDate())

    abstract fun previous(value: LocalDateTime): LocalDateTime
    abstract fun next(value: LocalDateTime): LocalDateTime
}