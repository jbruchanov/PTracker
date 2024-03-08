package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.atTimeZero
import com.scurab.ptracker.app.ext.minus
import kotlinx.datetime.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class DataFilter {
    All {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = { true }
    },
    ThisYear {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = { it.localDateTime.year == now.year }
    },
    ThisMonth {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = { it.localDateTime.year == now.year && it.localDateTime.monthNumber == now.monthNumber }
    },
    LastMonth {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = filter(now, 30.days)
    },
    Last3Months {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = filter(now, 90.days)
    },
    Last6Months {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = filter(now, 180.days)
    },
    LastYear {
        override fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean = filter(now, 365.days)
    }, ;

    abstract fun filter(now: LocalDateTime): (GroupStatsSum) -> Boolean
}

private fun filter(now: LocalDateTime, duration: Duration): (GroupStatsSum) -> Boolean {
    val bottomEdge = now.minus(duration).atTimeZero()
    return { it.localDateTime >= bottomEdge }
}
