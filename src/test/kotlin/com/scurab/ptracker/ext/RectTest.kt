package com.scurab.ptracker.app.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.jupiter.api.Test
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

class RectTest {

    @Test
    fun scalePivotZero1() {
        val rect = Rect(0f, 100f, 100f, 0f)
            .scale(2f, 0.5f, pivot = Offset.Zero)
        val expected = Rect(0f, 50f, 200f, 0f)
        assertEquals(expected, rect)
    }

    @Test
    fun scalePivotCenter() {
        val r1 = Rect(-50f, 50f, 50f, -50f)
        val rect = r1.scale(2f, 0.5f, pivot = r1.center)
        val expected = Rect(-100f, 25f, 100f, -25f)
        assertEquals(expected, rect)
    }

    @Test
    fun scalePivotRight1() {
        val r1 = Rect(0f, 100f, 100f, 0f)
        val rect = r1.scale(2f, 0.5f, pivot = Offset(r1.right, r1.nHeight / 2))
        val expected = Rect(100.0f, 25.0f, 300.0f, -25.0f)
        assertEquals(expected, rect)
    }

    @Test
    fun scalePivotRight2() {
        val r1 = Rect(0f, 100f, 100f, 0f)
        val rect = r1.scale(2f, 0.5f, pivot = Offset(r1.right, 0f))
        val expected = Rect(100f, 50f, 300f, 0f)
        assertEquals(expected, rect)
    }
}
