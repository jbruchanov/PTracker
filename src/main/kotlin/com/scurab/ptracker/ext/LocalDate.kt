package com.scurab.ptracker.ext

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.groupingYMD(): Long = (year * 1_00_00L) + (monthNumber * 1_00L) + dayOfMonth