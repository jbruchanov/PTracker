package com.scurab.ptracker.model

//might be saved into map as a key, ensure it's handled properly
sealed class Filter<T>(private val predicate: (T) -> Boolean) : (T) -> Boolean {
    abstract class All<T> : Filter<T>({ true })
    object AllTransactions : All<Transaction>()
    object TradesOnly : Filter<Transaction>({ it is Transaction.Trade })

    override fun invoke(item: T): Boolean = predicate(item)
}