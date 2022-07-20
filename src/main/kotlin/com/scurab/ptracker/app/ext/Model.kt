package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.HasDateTime
import com.scurab.ptracker.app.model.WithCache

fun <T : HasDateTime> T.groupValue(strategy: DateGrouping): Long {
    return if (this is WithCache) {
        //can't be done as 2 separated functions because of generics as receiver
        getOrPut("GroupStrategy:${strategy.name}") { strategy.toLongGroup(dateTime) }
    } else {
        strategy.toLongGroup(dateTime)
    }
}