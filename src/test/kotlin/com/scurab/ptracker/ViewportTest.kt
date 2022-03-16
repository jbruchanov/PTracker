package com.scurab.ptracker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.scurab.ptracker.ui.priceboard.PriceBoardState
import org.junit.jupiter.api.Test
import test.TestDensity
import kotlin.test.assertEquals

class ViewportTest {

    @Test
    fun test() {
        val state = PriceBoardState(emptyList(), TestDensity)
        state.canvasSize = Size(1000f, 500f)
        val rect = state.viewport()
        assertEquals(0f, rect.left, 1e-6f)
        assertEquals(0f, rect.bottom, 1e-6f)
        assertEquals(1000f, rect.right, 1e-6f)
        assertEquals(500f, rect.top, 1e-6f)
    }

    @Test
    fun test1() {
        val state = PriceBoardState(emptyList(), TestDensity)
        state.canvasSize = Size(1000f, 500f)
        state.offset = Offset(-100f, -100f)
        val rect = state.viewport()
        val expected = Rect(0f, state.canvasSize.height, state.canvasSize.width, 0f)
            .translate(offset = state.offset)

        assertEquals(expected.left, rect.left, 1e-6f)
        assertEquals(expected.bottom, rect.bottom, 1e-6f)
        assertEquals(expected.right, rect.right, 1e-6f)
        assertEquals(expected.top, rect.top, 1e-6f)
    }

    @Test
    fun test2() {
        val state = PriceBoardState(emptyList(), TestDensity)
        state.canvasSize = Size(1000f, 500f)
        state.scale = Offset(2f, 0.5f)
        val rect = state.viewport()
        val expected = Rect(470f, 750f, 970f, -250f)

        assertEquals(expected.left, rect.left, 1e-6f)
        assertEquals(expected.bottom, rect.bottom, 1e-6f)
        assertEquals(expected.right, rect.right, 1e-6f)
        assertEquals(expected.top, rect.top, 1e-6f)
    }
}

