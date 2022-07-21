package com.scurab.ptracker.ui

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter


object DateTimeFormats {
    val fullDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
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
    val debugFullDateTime = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val debugFullDate = DateTimeFormatter.ofPattern("yyyyMMdd")
    fun fullDateTime2Lines(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullDateTime2Lines)
    fun fullTime(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullTime)
}


