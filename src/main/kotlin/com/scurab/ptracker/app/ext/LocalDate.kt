package com.scurab.ptracker.app.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalTime

fun LocalDateTime.groupingYMD(): Long = (year * 1_00_00L) + (monthNumber * 1_00L) + dayOfMonth

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
fun nowInstant(): Instant = Clock.System.now()
fun Long.toLocalDateTime(tz: TimeZone = TimeZone.currentSystemDefault()) = Instant.fromEpochMilliseconds(this).toLocalDateTime(tz)
fun LocalDateTime.toLong() = this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
fun Instant.toLong() = this.toEpochMilliseconds()


fun LocalDateTime.withDayOfWeek(targetDayOfWeek: java.time.DayOfWeek): LocalDateTime {
    val daysDiff = this.dayOfWeek.value - targetDayOfWeek.value
    return this.toJavaLocalDateTime().minusDays(daysDiff.toLong()).with(LocalTime.of(0, 0, 0, 0)).toKotlinLocalDateTime()
}