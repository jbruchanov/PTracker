package com.scurab.ptracker.ext

import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.HasDateTime
import com.scurab.ptracker.model.WithCache

fun <T : HasDateTime> T.groupValue(strategy: GroupStrategy): Long {
    return if (this is WithCache) {
        //can't be done as 2 separated functions because of generics as receiver
        getOrPut("GroupStrategy:${strategy.name}") { strategy.groupingKey(dateTime) }
    } else {
        strategy.groupingKey(dateTime)
    }
}