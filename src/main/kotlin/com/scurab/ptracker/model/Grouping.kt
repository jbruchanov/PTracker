package com.scurab.ptracker.model

import com.scurab.ptracker.ext.groupingYMD
import kotlinx.datetime.LocalDateTime

enum class Grouping(val groupingKey: (LocalDateTime) -> Long) {
    Day({ it.groupingYMD() })
}