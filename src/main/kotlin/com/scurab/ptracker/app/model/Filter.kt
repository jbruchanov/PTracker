package com.scurab.ptracker.app.model

import kotlinx.datetime.LocalDateTime

//might be saved into map as a key, ensure it's handled properly
sealed class Filter<T>(private val predicate: (T) -> Boolean) : (T) -> Boolean {
    abstract class All<T> : Filter<T>({ true })
    object AllTransactions : All<Transaction>()
    object ImportantTransactions : Filter<Transaction>({ it.isImportant })
    data class DateFilter(val from: LocalDateTime, val to: LocalDateTime) : Filter<Transaction>({ from <= it.dateTime && it.dateTime < to })

    override fun invoke(item: T): Boolean = predicate(item)
}
