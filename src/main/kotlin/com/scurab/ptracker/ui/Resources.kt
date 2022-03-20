package com.scurab.ptracker.ui

import java.time.format.DateTimeFormatter


object DateFormats {
    val fullDate = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val year = DateTimeFormatter.ofPattern("yyyy")
    val monthMid = DateTimeFormatter.ofPattern("MMM")
    val monthYear = DateTimeFormatter.ofPattern("MM/yyyy")
    val dayNumber = DateTimeFormatter.ofPattern("d")
}


