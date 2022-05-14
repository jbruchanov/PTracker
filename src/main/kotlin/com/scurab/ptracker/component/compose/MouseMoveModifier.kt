package com.scurab.ptracker.component.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.scurab.ptracker.app.ext.times

fun Modifier.onMouseMove(block: ((Float, Float, IntSize) -> Unit)? = null): Modifier {
    block ?: return this
    return pointerInput(null) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val position = event.changes.first().position
                when (event.type) {
                    PointerEventType.Move -> block(position.x / size.width, position.y / size.height, size)
                    PointerEventType.Exit -> block(Float.NaN, Float.NaN, size)
                }
            }
        }
    }
}

fun Modifier.onMouseMove(items: Int, block: (Offset, Int) -> Unit): Modifier {
    return onMouseMove { x, y, size ->
        val isValid = !x.isNaN() && !y.isNaN()
        val index = if (isValid) {
            val hStep = (size.width / items) / 2f / size.width
            ((x + hStep) * items - 1).toInt().coerceAtMost(items - 1)
        } else {
            -1
        }
        block(Offset(x, y) * size, index)
    }
}