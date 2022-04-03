package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.GroupStrategy
import com.scurab.ptracker.app.model.HasDateTime
import com.scurab.ptracker.app.model.WithCache

fun <T : HasDateTime> T.groupValue(strategy: GroupStrategy): Long {
    return if (this is WithCache) {
        //can't be done as 2 separated functions because of generics as receiver
        getOrPut("GroupStrategy:${strategy.name}") { strategy.groupingKey(dateTime) }
    } else {
        strategy.groupingKey(dateTime)
    }
}