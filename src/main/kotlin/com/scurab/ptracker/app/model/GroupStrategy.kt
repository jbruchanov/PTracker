package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.groupingYMD
import kotlinx.datetime.LocalDateTime

enum class GroupStrategy(val groupingKey: (LocalDateTime) -> Long) {
    Day({ it.groupingYMD() })
}