package com.scurab.ptracker.component.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.scurab.ptracker.app.ext.times
import com.scurab.ptracker.component.util.lerp
import kotlin.math.roundToInt

private fun Modifier.onMouseMove(key: Any?, block: ((Float, Float, IntSize) -> Unit)? = null): Modifier {
    block ?: return this
    return pointerInput(key) {
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
    return onMouseMove(items) { x, y, size ->
        val isValid = !x.isNaN() && !y.isNaN()
        val index = if (isValid) lerp(0f, items - 1f, x).roundToInt() else -1
        block(Offset(x, y) * size, index)
    }
}