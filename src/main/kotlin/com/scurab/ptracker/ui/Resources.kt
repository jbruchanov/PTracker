package com.scurab.ptracker.ui

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.util.Locale


object DateTimeFormats {
    var twoFirstLettersOfDayName: Map<Long, String> = DayOfWeek.values().associateBy(
        keySelector = { it.value.toLong() },
        valueTransform = { it.getDisplayName(TextStyle.FULL, Locale.getDefault()).let { name -> name[0].uppercase() + name[1].lowercase() } }
    )
    val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fullDateWithDay = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .appendLiteral(' ')
        .appendText(ChronoField.DAY_OF_WEEK, twoFirstLettersOfDayName)
        .toFormatter()
    val fullDateTimeWithDayName = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd")
        .appendLiteral(' ')
        .appendPattern("HH:mm:ss")
        .appendLiteral(' ')
        .appendText(ChronoField.DAY_OF_WEEK, twoFirstLettersOfDayName)
        .toFormatter()
    val dayFullDate = DateTimeFormatterBuilder()
        .appendText(ChronoField.DAY_OF_WEEK, twoFirstLettersOfDayName)
        .appendLiteral(' ')
        .appendPattern("yyyy-MM-dd")
        .toFormatter()
    val year = DateTimeFormatter.ofPattern("yyyy")
    val monthMid = DateTimeFormatter.ofPattern("MMM")
    val monthYear = DateTimeFormatter.ofPattern("MM/yyyy")
    val yearWeek = DateTimeFormatter.ofPattern("yyyy/ww")
    val yearMonthMid = DateTimeFormatter.ofPattern("yyyy/MMM")
    val yearMonthFull = DateTimeFormatter.ofPattern("yyyy MMMM")
    val dayNumber = DateTimeFormatter.ofPattern("d")

    val fullTime = DateTimeFormatter.ofPattern("HH:mm:ss")
    val fullDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val fullDateTime2Lines = DateTimeFormatter.ofPattern("yyyy-MM-dd\nHH:mm:ss")
    fun fullDateTime(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullDateTime)
    fun fullDateTimeWithDay(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullDateTimeWithDayName)
    val debugFullDateTime = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val debugFullDate = DateTimeFormatter.ofPattern("yyyyMMdd")
    fun fullDateTime2Lines(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullDateTime2Lines)
    fun fullTime(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullTime)
}


