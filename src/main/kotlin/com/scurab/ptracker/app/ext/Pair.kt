package com.scurab.ptracker.app.ext

fun <T> Pair<T, T>.sameElseSwap(predicate: Boolean) = if(predicate) this else Pair(second, first)
fun <T> Pair<T, T>.firstIf(predicate: Boolean) = if(predicate) first else second
fun <T> Pair<T, T>.other(value: T) = if(first != value) first else second