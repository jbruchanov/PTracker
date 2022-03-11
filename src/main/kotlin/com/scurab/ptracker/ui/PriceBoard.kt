@file:OptIn(ExperimentalComposeUiApi::class)

package com.scurab.ptracker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.scurab.ptracker.ext.Size
import com.scurab.ptracker.ext.f0
import com.scurab.ptracker.ext.f3
import com.scurab.ptracker.ext.filterVisible
import com.scurab.ptracker.ext.heightAbs
import com.scurab.ptracker.ext.normalize
import com.scurab.ptracker.ext.scale
import com.scurab.ptracker.ext.toLTRBWH
import com.scurab.ptracker.ext.transformNormToReal
import com.scurab.ptracker.ext.transformNormToViewPort
import com.scurab.ptracker.ext.translate
import com.scurab.ptracker.ext.withTranslateAndScale
import com.scurab.ptracker.model.PriceItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextLine
import java.awt.Cursor
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

object PriceDashboardConfig {
    const val SnappingMouseCrossHorizontally = true
}

class PriceBoardState {
    var scale by mutableStateOf(ONE)
    var offset by mutableStateOf(Offset.Zero)
    var pointer by mutableStateOf(Point.ZERO)
    var canvasSize by mutableStateOf(Size.Zero)

    //right, height/2
    fun chartScaleOffset() = Offset(-offset.x + canvasSize.width, -offset.y + canvasSize.height / 2)
    fun viewport() = Rect(0f, canvasSize.height, canvasSize.width, 0f)
        //offset & height (to move origin to bottom/left)
        .translate(offset.x, -canvasSize.height + offset.y)
        //scale in same way as we do for preview
        .scale(1f / scale.x, 1f / scale.y, pivot = chartScaleOffset().translate(y = -canvasSize.height))
        //flip x axis to have bottomLeft negavite, topRight positive
        .scale(-1f, 1f)

    fun viewportPointer() = pointer.normalize(canvasSize).transformNormToViewPort(viewport())
    fun normalizedPointer() = pointer.normalize(canvasSize)
    fun selectedPriceItemIndex() = ceil(viewportPointer().x / PriceDashboardSizes.PriceItemWidth).toInt() - 1

    suspend fun reset(animate: Boolean = true) = coroutineScope {
        if (animate) {
            launch { Animatable(scale, Offset.VectorConverter).animateTo(ONE, animationSpec = tween(1000)) { scale = value } }
            launch { Animatable(offset, Offset.VectorConverter).animateTo(Offset.Zero, animationSpec = tween(500)) { offset = value } }
        } else {
            scale = ONE
            offset = Offset.Zero
        }
    }

    companion object {
        private val ONE = Offset(1f, 1f)
    }
}


@Composable
fun PriceBoard(items: List<PriceItem>) {
    val state = remember { PriceBoardState() }

    Box(
        modifier = Modifier
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR)))
            .background(PriceDashboardColor.Background)
            .onSizeChanged {
                if (!state.canvasSize.isEmpty()) {
                    val diffX = it.width - state.canvasSize.width
                    state.offset = state.offset.translate(diffX, 0f)
                }
                state.canvasSize = Size(it.width, it.height)
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move) {
                            val moveEvent = event.changes.first()
                            state.pointer = Point(moveEvent.position.x, moveEvent.position.y)
                        }
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    state.offset = state.offset.translate(dragAmount.x / state.scale.x, dragAmount.y / state.scale.y)
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val scrollEvent = event.changes.first()
                            val scrollDelta = scrollEvent.scrollDelta
                            state.scale = Offset(
                                (state.scale.x + (scrollDelta.x / 20f)).coerceIn(0.01f, 4f),
                                (state.scale.y + (scrollDelta.y / 50f)).coerceIn(0.01f, 4f)
                            )
                            scrollEvent.consume()
                        }
                    }
                }
            }
            .fillMaxSize()
    ) {
        val scope = rememberCoroutineScope()
        PriceBoardGrid(state)
        PriceBoardPrices(items, state)
        PriceAxis(items, state)
        PriceBoardMouseCross(state)
        PriceBoardDebug(items, state)
        Button(modifier = Modifier.align(Alignment.TopEnd), onClick = { scope.launch { state.reset() } }) {
            Text("R")
        }
    }
}


@Composable
private fun PriceBoardPrices(items: List<PriceItem>, state: PriceBoardState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        withTranslateAndScale(state) {
            items.filterVisible(state).forEach { priceItem ->
                val x = priceItem.index * PriceDashboardSizes.PriceItemWidth
                val x2 = PriceDashboardSizes.PriceItemWidth / 2f
                //scaleY flipped as we want to have origin at left/Bottom
                translate(x, 0f) {
                    scale(1f, -1f) {
                        drawRect(priceItem.color, topLeft = Offset(0f, priceItem.rectOffsetY), size = priceItem.rectSize)
                        drawLine(
                            priceItem.color,
                            start = Offset(x2, priceItem.spikeOffsetY1),
                            end = Offset(x2, priceItem.spikeOffsetY2),
                            //keep the strokeWidth scale independent
                            strokeWidth = PriceDashboardSizes.SpikeLineStrokeWidth.toPx() / state.scale.x
                        )
                    }
                }
            }
            drawCircle(Color.Red, radius = 10f, center = Offset(0f, size.height))
        }
    }
}

@Composable
private fun PriceAxis(items: List<PriceItem>, state: PriceBoardState) {
    val metrics = remember { TextRendering.fontAxis.metrics }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = metrics.height + metrics.bottom
        val width = 60
        val path = Path().apply {
            moveTo(0f, size.height)
            lineTo(size.width, size.height)
            lineTo(size.width, 0f)
            lineTo(size.width - width, 0f)
            lineTo(size.width - width, size.height - height)
            lineTo(0f, size.height - height)
            close()
        }
        drawPath(path, PriceDashboardColor.BackgroundAxis)

        //Axis X
        translate(state.offset.x, size.height) {
            scale(state.scale.x, 1f, pivot = state.chartScaleOffset()) {
                val step = ceil(TextRendering.axisXStep / state.scale.x).toInt()
                items.filterVisible(state, step = step).forEach { priceItem ->
                    val x = (priceItem.index + 0.5f) * PriceDashboardSizes.PriceItemWidth
                    translate(x, 0f) {
                        scale(scaleX = 1f / state.scale.x, scaleY = 1f, pivot = Offset.Zero) {
                            drawIntoCanvas {
                                it.nativeCanvas.drawTextLine(TextLine.make(priceItem.renderDate, TextRendering.fontAxis), 0f, -metrics.bottom, TextRendering.paint)
                            }
                        }
                    }
                }
            }
        }

        //Axis Y
        val viewport = state.viewport().scale(-1f, -1f)
        val minPrice = -viewport.top
        val maxPrice = minPrice + viewport.heightAbs
        val steps = floor(state.canvasSize.height / TextRendering.font.metrics.height).toInt()
        val priceStep = (maxPrice - minPrice) / steps.toFloat()
        val offsetYStep = state.canvasSize.height / steps
        (2 until steps - 1).forEach { step ->
            val price = minPrice + ((steps - step) * priceStep)
            val text = TextLine.make(price.f0, TextRendering.fontAxis)
            translate(left = size.width - text.width - PriceDashboardSizes.AxisPadding.toPx(), step * offsetYStep) {
                drawIntoCanvas {
                    it.nativeCanvas.drawTextLine(text, 0f, -metrics.bottom + text.height / 2, TextRendering.paint)
                }
            }
        }
    }
}

@Composable
private fun PriceBoardGrid(state: PriceBoardState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasSize = size
        val gridLinesCount = 10
        //vertical lines
        var counter = 0
        //horizontal lines
        counter = 0
        do {
            val yOffset = ((counter + 1f) / gridLinesCount) * canvasSize.height * state.scale.y
            translate(top = yOffset) {
                drawLine(
                    PriceDashboardColor.GridLine,
                    start = Offset(0f, 0f),
                    end = Offset(canvasSize.width, 0f),
                    strokeWidth = PriceDashboardSizes.GridLineStrokeWidth.toPx()
                )
            }
            counter++
        } while (yOffset < canvasSize.height)

        counter = 0
        drawIntoCanvas {
            do {
                val xOffset = (((counter + 1f) / gridLinesCount) * canvasSize.width * state.scale.x)
                translate(left = xOffset) {
                    drawLine(
                        PriceDashboardColor.GridLine,
                        start = Offset(0f, 0f),
                        end = Offset(0f, canvasSize.height),
                        strokeWidth = PriceDashboardSizes.GridLineStrokeWidth.toPx()
                    )
                    counter++

                    val text = TextLine.make("${counter}-3", TextRendering.font)
                    val textWidthHalf = (text.width / 2)
                    //it.nativeCanvas.drawTextLine(text, -textWidthHalf, canvasSize.height, textPaint)
                }
            } while (xOffset < canvasSize.width)
        }
    }
}

@Composable
private fun PriceBoardMouseCross(state: PriceBoardState) {
    val density = LocalDensity.current.density
    val effect = remember { PathEffect.dashPathEffect(floatArrayOf(10f * density, 10f * density)) }
    Canvas(modifier = Modifier.fillMaxSize()) {
        //horizontal
        drawLine(
            PriceDashboardColor.MouseCross,
            start = Offset(0f, state.pointer.y),
            end = Offset(size.width, state.pointer.y),
            strokeWidth = PriceDashboardSizes.MouseCrossStrokeWidth.toPx(),
            pathEffect = effect
        )

        //vertical
        val colWidth = PriceDashboardSizes.PriceItemWidth
        val x = if (PriceDashboardConfig.SnappingMouseCrossHorizontally) {
            val colWidthHalf = colWidth / 2f
            val viewPort = state.viewport()
            val vPointer = state.normalizedPointer().transformNormToViewPort(viewPort)
            val x = (((vPointer.x + colWidthHalf) / colWidth).roundToInt() * colWidth) - colWidthHalf
            Point(x, 0f)
                .normalize(viewPort)
                .transformNormToReal(size)
                .x
        } else state.pointer.x
        drawLine(
            PriceDashboardColor.MouseCross,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = PriceDashboardSizes.MouseCrossStrokeWidth.toPx(),
            pathEffect = effect
        )
    }
}

@Composable
private fun PriceBoardDebug(items: List<PriceItem>, state: PriceBoardState) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val viewPort = state.viewport()
        val canvasSize = size
        val nPointer = state.normalizedPointer()
        val vPointer = nPointer.transformNormToViewPort(viewPort)
        val nPointer2 = vPointer.normalize(viewPort)
        val rPointer = nPointer2.transformNormToReal(canvasSize)

        val rows = listOf(
            "Offset:[${state.offset.x.toInt()}, ${state.offset.y.toInt()}]",
            "Mouse:[${state.pointer.x.toInt()}, ${(canvasSize.height - state.pointer.y).toInt()}] " +
                    "N[${nPointer.x.f3}, ${nPointer.y.f3}] =>" +
                    "V[${vPointer.x.f3}, ${vPointer.y.f3}] => " +
                    "N[${nPointer2.x.f3}, ${nPointer2.y.f3}] =>" +
                    "R[${rPointer.x.f3}, ${rPointer.y.f3}]",
            "Canvas:[${canvasSize.width.toInt()},${canvasSize.height.toInt()}]",
            "Scale:[${state.scale.x.f3},${state.scale.y.f3}]",
            "ViewPort:[${viewPort.toLTRBWH()}]",
            "Index:[${state.selectedPriceItemIndex()}]",
        )
        drawIntoCanvas {
            translate(left = 2f, top = TextRendering.font.metrics.height) {
                rows.forEachIndexed { index, s ->
                    it.nativeCanvas.drawTextLine(TextLine.Companion.make(s, TextRendering.font), 0f, index * TextRendering.font.metrics.height, TextRendering.paint)
                }
            }
        }
        withTranslateAndScale(state) {
            translate(top = canvasSize.height) {
                drawLine(PriceDashboardColor.Debug, start = Offset(-canvasSize.width, 0f), end = Offset(canvasSize.width, 0f))
                drawLine(PriceDashboardColor.Debug, start = Offset(0f, -canvasSize.height), end = Offset(0f, canvasSize.height))
            }
        }
    }
}