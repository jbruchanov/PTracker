@file:OptIn(ExperimentalComposeUiApi::class)

package com.scurab.ptracker.ui.priceboard

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.scurab.ptracker.ext.translate
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.AppTheme.DashboardSizes
import org.jetbrains.skia.Point
import kotlin.math.sign

private fun Offset.scrollOffset(scrollX: Float, scrollY: Float) = Offset(
    (x + (x * 0.1f * scrollX.sign)).coerceIn(PriceDashboardConfig.ScaleRangeX[0], PriceDashboardConfig.ScaleRangeX[1]),
    (y + (y * 0.025f * scrollY.sign)).coerceIn(PriceDashboardConfig.ScaleRangeY[0], PriceDashboardConfig.ScaleRangeY[1]),
)

internal fun Modifier.onMouseDrag(state: PriceBoardState): Modifier {
    return pointerInput(state) {
        detectDragGestures { change, dragAmount ->
            if (!state.isChangingScale) {
                change.consumeAllChanges()
                val diff = Offset(-dragAmount.x / state.scale.x, dragAmount.y / state.scale.y)
                val newOffset = state.offset.translate(diff.x, diff.y)
                state.offset = newOffset
            }
        }
    }
}

@Composable
internal fun Modifier.onSizeChange(state: PriceBoardState): Modifier {
    return onSizeChanged { intSize ->
        val size = intSize.toSize()
        if (state.canvasSize == size) return@onSizeChanged
        if (!state.canvasSize.isEmpty()) {
            val diffX = intSize.width - state.canvasSize.width
            state.offset = state.offset.translate(-diffX, 0f)
            state.pointer = Point.ZERO
        }
        state.canvasSize = size
    }
}

internal fun Modifier.onWheelScroll(state: PriceBoardState): Modifier {
    return pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                    val scrollEvent = event.changes.first()
                    val isInVerticalAxis = scrollEvent.position.x > state.verticalPriceBarLeft()
                    val isInHorizontalAxis = scrollEvent.position.y > state.bottomAxisBarHeight()
                    val scrollDelta = scrollEvent.scrollDelta
                    var scrollX = 0f
                    var scrollY = 0f
                    val offsetX = scrollDelta.x * 5 / state.scale.x * DashboardSizes.PriceItemWidth
                    when {
                        isInVerticalAxis -> scrollY = scrollDelta.y
                        else -> scrollX = scrollDelta.y
                    }
                    state.scale = state.scale.scrollOffset(scrollX, scrollY)
                    state.offset = state.offset.translate(offsetX)
                    scrollEvent.consume()
                }
            }
        }
    }
}

internal fun Modifier.onMouseMove(state: PriceBoardState): Modifier {
    return pointerInput(state) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.first()
                state.pointer = Point(change.position.x, change.position.y)
                val isInVerticalAxisZone = state.verticalPriceBarLeft() < state.pointer.x
                when (event.type) {
                    PointerEventType.Press -> {
                        state.isChangingScale = isInVerticalAxisZone
                    }
                    PointerEventType.Move -> {
                        state.isChangingScale = state.isChangingScale && change.pressed
                        state.mouseIcon = if (isInVerticalAxisZone) AppTheme.MouseCursors.PointerIconResizeVertically else AppTheme.MouseCursors.PointerIconCross
                        if (state.isChangingScale) {
                            val diff = change.previousPosition.y - change.position.y
                            state.scale = state.scale.scrollOffset(0f, diff)
                        }
                    }
                    PointerEventType.Exit -> {
                        state.isChangingScale = false
                        state.pointer = Point.ZERO
                    }
                }
                state.pointedPriceItem = state.items.getOrNull(state.selectedPriceItemIndex())
            }
        }
    }
}