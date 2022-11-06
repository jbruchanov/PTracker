package com.scurab.ptracker.app.ext

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CollectionsKtTest {

    @Test
    fun takeAround() {
        val data = data(20)

        assertEquals(data(0), data.takeAround(5, 0))
        assertEquals(data(1, valueOffset = 5), data.takeAround(5, 1))
        assertEquals(data(20), data.takeAround(5, 20))

        assertEquals(data(10), data.takeAround(0, 10))
        assertEquals(data(10), data.takeAround(5, 10))
        assertEquals(data.slice(5 until 15), data.takeAround(10, 10))
        assertEquals(data.takeLast(10), data.takeAround(19, 10))
    }

    private fun data(items: Int, valueOffset: Int = 0) = IntArray(items) { valueOffset + it }.toList()
}
