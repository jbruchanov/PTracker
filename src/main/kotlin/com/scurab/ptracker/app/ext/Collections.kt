package com.scurab.ptracker.app.ext

import com.scurab.ptracker.app.model.GroupStrategy
import com.scurab.ptracker.app.model.HasDateTime
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min


fun List<HasDateTime>.firstIndexOf(other: HasDateTime, grouping: GroupStrategy): Int {
    val v = grouping.groupingKey(other.dateTime)
    return indexOfFirst { grouping.groupingKey(it.dateTime) == v }
}

fun <T> List<T>.takeAround(index: Int, n: Int): List<T> {
    require(index in indices) { "Invalid index:$index, must be in indices:${indices}" }
    val l = floor(n / 2f).toInt()
    val r = ceil(n / 2f).toInt()
    val le = index - l
    val re = index + r
    return when {
        n == 0 -> emptyList()
        n == 1 -> listOf(get(index))
        n >= size -> this.toList()
        le >= 0 && re < size -> this.subList(le, re)
        le < 0 -> subList(0, (re - le).coerceAtLeast(min(n, size)))
        re > size -> subList((le - (re - size)).coerceAtLeast(0), size)
        else -> TODO("size:$size, index:$index, n:$n")
    }
}

/**
 * Execute "map" operation on collection in parallel.
 *
 * @param I
 * @param O
 * @param parallelism - number indicates how many map ops can be run in altogether
 * @param map
 * @return
 */
suspend fun <I, O> Collection<I>.parallelMapIndexed(parallelism: Int = 4, map: suspend (Int, I) -> O): List<O> {
    require(parallelism > 0) { "Invalid parallelism:$parallelism, must be at least 1" }
    val input = Channel<Pair<Int, I>>()
    val result = ArrayList<O>(size).also {
        @Suppress("UNCHECKED_CAST")
        //fill the arrayList with nulls, so we can call simple set[i] = value later in map
        //to preserve the order of elements, this list is writeOnly as this breaks nullability check!
        (it as ArrayList<Any?>).let { nullable ->
            repeat(size) { nullable.add(null) }
        }
    }

    coroutineScope {
        launch {
            forEachIndexed { i, v -> input.send(Pair(i, v)) }
            input.close()
        }

        (0 until parallelism).map {
            launch {
                for ((i, v) in input) {
                    result[i] = map(i, v)
                }
            }
        }.joinAll()
    }
    return result
}

