@file:OptIn(ExperimentalComposeUiApi::class)

package com.scurab.ptracker.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.scurab.ptracker.ext.translate
import kotlinx.coroutines.launch
import org.jetbrains.skia.Point

internal fun Modifier.onMouseDrag(state: PriceBoardState): Modifier {
    return pointerInput(Unit) {
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
    val scope = rememberCoroutineScope()
    return onSizeChanged { intSize ->
        val size = intSize.toSize()
        if (state.canvasSize == size) return@onSizeChanged
        if (state.canvasSize.isEmpty()) {
            scope.launch {
                val viewport = state.initViewport(size)
                state.setViewport(viewport, size)
            }
        } else {
            val diffX = intSize.width - state.canvasSize.width
            state.offset = state.offset.translate(-diffX, 0f)
            state.pointer = Point.ZERO
        }
        state.canvasSize = size
    }
}

internal fun Modifier.onWheelScroll(state: PriceBoardState): Modifier {
    return pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                if (event.type == PointerEventType.Scroll) {
                    val scrollEvent = event.changes.first()
                    val isInVerticalAxis = scrollEvent.position.x > state.verticalPriceBarLeft()
                    val isInHorizontalAxis = scrollEvent.position.y > state.bottomAxisBarHeight()

                    val scrollDelta = scrollEvent.scrollDelta
                    var scaleX = 0f
                    var scaleY = 0f
                    val offsetX = scrollDelta.x * 5 * PriceDashboardSizes.PriceItemWidth
                    when {
                        isInVerticalAxis -> scaleY = scrollDelta.y
                        else -> scaleX = scrollDelta.y
                    }

                    state.scale = Offset(
                        (state.scale.x + (scaleX / 20f)).coerceIn(PriceDashboardConfig.ScaleRangeX[0], PriceDashboardConfig.ScaleRangeX[1]),
                        (state.scale.y + (scaleY / 50f)).coerceIn(PriceDashboardConfig.ScaleRangeY[0], PriceDashboardConfig.ScaleRangeY[1]),
                    )
                    state.offset = state.offset.translate(offsetX)
                    scrollEvent.consume()
                }
            }
        }
    }
}

internal fun Modifier.onMouseMove(state: PriceBoardState): Modifier {
    return pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.first()
                state.pointer = Point(change.position.x, change.position.y)
                val isInVerticalAxisZone = state.verticalPriceBarLeft() < state.pointer.x
                if (event.type == PointerEventType.Press) {
                    state.isChangingScale = isInVerticalAxisZone
                } else if (event.type == PointerEventType.Move) {
                    state.isChangingScale = state.isChangingScale && change.pressed
                    state.mouseIcon = if (isInVerticalAxisZone) MouseCursors.PointerIconResizeVertically else MouseCursors.PointerIconCross
                    if (state.isChangingScale) {
                        val diff = change.previousPosition.y - change.position.y
                        //TODO common logic for boundaries
                        state.scale = state.scale.copy(y = (state.scale.y + (diff / 1000f)).coerceIn(0.01f, 4f))
                    }
                }
                state.pointedPriceItem = state.items.getOrNull(state.selectedPriceItemIndex())
            }
        }
    }
}