package com.scurab.ptracker.ext

fun <T> Pair<T, T>.sameElseSwap(predicate: Boolean) = if(predicate) this else Pair(second, first)