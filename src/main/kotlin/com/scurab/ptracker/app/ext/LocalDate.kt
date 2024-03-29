package com.scurab.ptracker.app.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.LocalTime
import kotlin.time.Duration

fun LocalDateTime.groupingYMD(): Long = (year * 1_00_00L) + (monthNumber * 1_00L) + dayOfMonth
fun LocalDateTime.groupingYD(): Long = (year * 1000L) + dayOfYear
fun LocalDateTime.groupingYM(): Long = (year * 1_00L) + monthNumber

fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
fun nowInstant(): Instant = Clock.System.now()
fun Long.toLocalDateTime(tz: TimeZone = TimeZone.currentSystemDefault()) = Instant.fromEpochMilliseconds(this).toLocalDateTime(tz)
fun LocalDateTime.toLong() = this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
fun Instant.toLong() = this.toEpochMilliseconds()

fun LocalDateTime.withDayOfWeek(targetDayOfWeek: java.time.DayOfWeek): LocalDateTime {
    val daysDiff = this.dayOfWeek.value - targetDayOfWeek.value
    return this.toJavaLocalDateTime().minusDays(daysDiff.toLong()).with(LocalTime.of(0, 0, 0, 0)).toKotlinLocalDateTime()
}

fun LocalDateTime.atTimeZero() = this.date.atTime(0, 0, 0, 0)

fun LocalDate.atDayOfMonth(dayOfMonth: Int) = LocalDate(year, month, dayOfMonth)

fun LocalDateTime.minus(duration: Duration) = toInstant(TimeZone.UTC).minus(duration).toLocalDateTime(TimeZone.UTC)
