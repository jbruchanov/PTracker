package com.scurab.ptracker.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun LocalDateTime.groupingYMD(): Long = (year * 1_00_00L) + (monthNumber * 1_00L) + dayOfMonth

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
fun Long.toLocalDateTime(tz: TimeZone = TimeZone.currentSystemDefault()) = Instant.fromEpochMilliseconds(this).toLocalDateTime(tz)
fun LocalDateTime.toLong() = this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
fun Instant.toLong() = this.toEpochMilliseconds()