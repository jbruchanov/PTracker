package com.scurab.ptracker.ui

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter


object DateTimeFormats {
    val fullDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val year = DateTimeFormatter.ofPattern("yyyy")
    val monthMid = DateTimeFormatter.ofPattern("MMM")
    val monthYear = DateTimeFormatter.ofPattern("MM/yyyy")
    val dayNumber = DateTimeFormatter.ofPattern("d")

    val fullDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    fun fullDateTime(localDateTime: LocalDateTime): String = localDateTime.toJavaLocalDateTime().format(fullDateTime)
}


