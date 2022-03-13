@file:OptIn(ExperimentalComposeUiApi::class)

package com.scurab.ptracker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.scurab.ptracker.ext.f3
import com.scurab.ptracker.ext.filterVisible
import com.scurab.ptracker.ext.heightAbs
import com.scurab.ptracker.ext.nativeCanvas
import com.scurab.ptracker.ext.normalize
import com.scurab.ptracker.ext.priceRound
import com.scurab.ptracker.ext.scale
import com.scurab.ptracker.ext.scale2
import com.scurab.ptracker.ext.size
import com.scurab.ptracker.ext.toLTRBWH
import com.scurab.ptracker.ext.toPx
import com.scurab.ptracker.ext.transformNormToReal
import com.scurab.ptracker.ext.transformNormToViewPort
import com.scurab.ptracker.ext.translate
import com.scurab.ptracker.ext.widthAbs
import com.scurab.ptracker.ext.withTranslateAndScale
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.priceDetails
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.skia.FontMetrics
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextLine
import java.awt.Cursor
import java.lang.Float.min
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

object PriceDashboardConfig {
    const val SnappingMouseCrossHorizontally = true
    const val AxisYContentCoef = 0.5f
}

class PriceBoardState(items: List<PriceItem>) {
    var scale by mutableStateOf(ONE)
    var offset by mutableStateOf(Offset.Zero)
    var pointer by mutableStateOf(Point.ZERO)
    var canvasSize by mutableStateOf(Size.Zero)
    var pointedPriceItem by mutableStateOf<PriceItem?>(null)
    val items by mutableStateOf(items)

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
            animateToOffsetScale(offset = Offset.Zero, scale = ONE)
        } else {
            scale = ONE
            offset = Offset.Zero
        }
    }

    fun initviewPort(size: Size, density: Float): Rect {
        val lastItem = items.lastOrNull() ?: return Rect(0f, 0f, size.width, size.height)
        val allColumnsWidth = (items.size * PriceDashboardSizes.PriceItemWidth)
        val y = (lastItem.open + lastItem.close).toFloat() / 2f
        return Rect(0f, size.height, size.width, 0f)
            .translate(size.width - PriceDashboardSizes.VerticalPriceBarWidth.toPx(density), -size.height / 2)
            .scale(1f, 1f/*, pivot = Offset(size.width, size.height / 2)*/)
            .translate(-allColumnsWidth, y)
    }

    suspend fun animateToViewport(viewport: Rect, size: Size = this.canvasSize) {
        require(!size.isEmpty()) { "Size has 0 values" }
        val offset = Offset(viewport.left, viewport.bottom)
        val scale = Offset(viewport.widthAbs / size.width, viewport.heightAbs / size.height)
        animateToOffsetScale(offset, scale)
    }

    suspend fun animateToOffsetScale(offset: Offset = this.offset, scale: Offset = this.scale) = coroutineScope {
        launch { Animatable(this@PriceBoardState.offset, Offset.VectorConverter).animateTo(offset, animationSpec = tween(300)) { this@PriceBoardState.offset = value } }
        launch { Animatable(this@PriceBoardState.scale, Offset.VectorConverter).animateTo(scale, animationSpec = tween(300)) { this@PriceBoardState.scale = value } }
    }

    companion object {
        private val ONE = Offset(1f, 1f)
    }
}

private fun PriceBoardState.verticalSteps() = (floor(canvasSize.height / TextRendering.font.metrics.height).toInt() * PriceDashboardConfig.AxisYContentCoef).toInt()
private fun PriceBoardState.viewportColumnWidth() = PriceDashboardSizes.PriceItemWidth * scale.x
private fun PriceBoardState.columns() = (viewport().widthAbs / PriceDashboardSizes.PriceItemWidth).roundToInt()
private fun PriceBoardState.mousePrice() = normalizedPointer().transformNormToViewPort(viewport()).y
private fun PriceBoardState.bottomAxisBarHeight(density: Float, metrics: FontMetrics = TextRendering.fontLabels.metrics): Float =
    max(metrics.height + metrics.bottom, PriceDashboardSizes.BottomAxisContentMinHeight.toPx(density))

private fun PriceBoardState.verticalPriceBarLeft(density: Float): Float = canvasSize.width - PriceDashboardSizes.VerticalPriceBarWidth.toPx(density)
private fun PriceBoardState.verticalLabel(): String = mousePrice().roundToInt().toString()
private fun PriceBoardState.horizontalLabel(items: List<PriceItem>): String? = items.getOrNull(selectedPriceItemIndex())?.fullDate
private fun getHorizontalAxisText(items: List<PriceItem>, index: Int, step: Int): String {
    val item = items.getOrNull(index) ?: return ""
    val prev = items.getOrNull(index - step)
    //TODO: handle also smaller candles than day
    val formatter = when {
        prev == null -> DateFormats.monthYear
        item.time.year != prev.time.year -> DateFormats.year
        item.time.monthNumber != prev.time.monthNumber -> DateFormats.monthMid
        else -> DateFormats.dayNumber
    }
    return formatter.format(item.time.toJavaLocalDateTime())
}

fun PriceBoardState.viewportIndexes(startOffset: Int = 0, endOffset: Int = 0): IntProgression {
    val vp = viewport()
    val colWidth = PriceDashboardSizes.PriceItemWidth
    val firstIndex = floor(vp.left / colWidth).toInt()
    val lastIndex = firstIndex + ceil(vp.widthAbs / colWidth).toInt()
    return (firstIndex + startOffset) until (lastIndex + endOffset)
}

fun PriceItem.isVisible(state: PriceBoardState, viewport: Rect = state.viewport()): Boolean {
    val colWidth = PriceDashboardSizes.PriceItemWidth
    val firstIndex = (max(0f, viewport.left) / colWidth).toInt()
    val widthToFill = viewport.widthAbs + min(viewport.left, 0f)
    val lastIndex = firstIndex + widthToFill
    //TODO: add vertically
    return firstIndex <= index && index <= lastIndex
}


@Composable
fun PriceBoard(items: List<PriceItem>) {
    val scope = rememberCoroutineScope()
    val state = remember { PriceBoardState(items) }
    var mouseIcon by remember { mutableStateOf(PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR))) }
    var isChangingScale by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density
    Box(
        modifier = Modifier
            .pointerHoverIcon(mouseIcon)
            .background(PriceDashboardColor.Background)
            .onSizeChanged { intSize ->
                val size = intSize.toSize()
                if (state.canvasSize.isEmpty()) {
                    scope.launch {
                        val viewport = state.initviewPort(size, density)
                        state.animateToViewport(viewport, size)
                    }
                } else {
                    val diffX = intSize.width - state.canvasSize.width
                    state.offset = state.offset.translate(diffX, 0f)
                }
                state.canvasSize = size
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        state.pointer = Point(change.position.x, change.position.y)
                        val isInVerticalAxisZone = state.verticalPriceBarLeft(density) < state.pointer.x
                        if (event.type == PointerEventType.Press) {
                            isChangingScale = isInVerticalAxisZone
                        } else if (event.type == PointerEventType.Move) {
                            isChangingScale = isChangingScale && change.pressed
                            mouseIcon = if (isInVerticalAxisZone) MouseCursors.PointerIconResizeVertically else MouseCursors.PointerIconCross
                            if (isChangingScale) {
                                val diff = change.previousPosition.y - change.position.y
                                //TODO common logic for boundaries
                                state.scale = state.scale.copy(y = (state.scale.y + (diff / 1000f)).coerceIn(0.01f, 4f))
                            }
                        }
                        state.pointedPriceItem = items.getOrNull(state.selectedPriceItemIndex())
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    if(!isChangingScale) {
                        change.consumeAllChanges()
                        state.offset = state.offset.translate(dragAmount.x / state.scale.x, dragAmount.y / state.scale.y)
                    }
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
        PriceAxisGrid(items, state)
        PriceBoardPrices(items, state)
        PriceAxisBackground(state)
        PriceAxisContent(items, state)
        PriceBoardMouse(items, state)
        PriceAxisEdgeLines(state)
        PriceBoardDebug(items, state)
        Column(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-65).dp)) {
            Button(onClick = { scope.launch { state.reset() } }) {
                Text("R")
            }
            Button(onClick = { scope.launch { state.animateToViewport(state.initviewPort(state.canvasSize, density)) } }) {
                Text("I")
            }
        }

        PriceSelectedDayDetail(state.pointedPriceItem)
    }
}

@Composable
fun PriceSelectedDayDetail(item: PriceItem?) {
    if (item != null) {
        val text = remember(item) { item.priceDetails() }
        Text(
            text, modifier = Modifier
                .offset(8.dp, 4.dp)
                .background(PriceDashboardColor.BackgroundAxis, shape = RoundedCornerShape(2.dp))
                .padding(2.dp),
            color = PriceDashboardColor.OnBackground,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}


@Composable
private fun PriceBoardPrices(items: List<PriceItem>, state: PriceBoardState) {
    Canvas {
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
        }
    }
}

@Composable
private fun PriceAxisBackground(state: PriceBoardState) {
    val density = LocalDensity.current.density
    val bottomAxisHeight = state.bottomAxisBarHeight(density)
    val canvasSize = state.canvasSize
    val verticalPriceBarLeft = state.verticalPriceBarLeft(density)
    val axisBackgroundPath = remember(canvasSize) {
        Path().apply {
            moveTo(0f, canvasSize.height)
            lineTo(canvasSize.width, canvasSize.height)
            lineTo(canvasSize.width, 0f)
            lineTo(verticalPriceBarLeft, 0f)
            lineTo(verticalPriceBarLeft, canvasSize.height - bottomAxisHeight)
            lineTo(0f, canvasSize.height - bottomAxisHeight)
            close()
        }
    }
    Canvas {
        drawPath(axisBackgroundPath, PriceDashboardColor.BackgroundAxis)
    }
}

@Composable
private fun PriceAxisEdgeLines(state: PriceBoardState) {
    val density = LocalDensity.current.density
    val bottomAxisHeight = state.bottomAxisBarHeight(density)
    val canvasSize = state.canvasSize
    val verticalPriceBarLeft = state.verticalPriceBarLeft(LocalDensity.current.density)
    Canvas {
        drawLine(
            PriceDashboardColor.BackgroundAxisEdge,
            start = Offset(0f, canvasSize.height - bottomAxisHeight),
            end = Offset(verticalPriceBarLeft, canvasSize.height - bottomAxisHeight),
            strokeWidth = PriceDashboardSizes.GridLineStrokeWidth.toPx()
        )
        drawLine(
            PriceDashboardColor.BackgroundAxisEdge,
            start = Offset(verticalPriceBarLeft, 0f),
            end = Offset(verticalPriceBarLeft, canvasSize.height - bottomAxisHeight),
            strokeWidth = PriceDashboardSizes.GridLineStrokeWidth.toPx()
        )
    }
}


@Composable
private fun PriceAxisContent(items: List<PriceItem>, state: PriceBoardState) {
    val metrics = remember { TextRendering.fontAxis.metrics }
    val canvasSize = state.canvasSize
    val viewport = state.viewport().scale(-1f, -1f)

    PriceAxisContentTemplate(items, state,
        horizontalContent = { priceItem, step ->
            priceItem ?: return@PriceAxisContentTemplate
            val bottomAxisHeight = state.bottomAxisBarHeight(density)
            val label = getHorizontalAxisText(items, priceItem.index, step)
            val text = TextLine.make(label, TextRendering.fontAxis)
            nativeCanvas.drawTextLine(text, -text.width / 2, (-bottomAxisHeight + text.height - metrics.bottom) / 2, TextRendering.paint)
        },
        verticalContent = { step, steps ->
            val minPrice = -viewport.top
            val maxPrice = minPrice + viewport.heightAbs
            val priceStep = (maxPrice - minPrice) / steps.toFloat()
            val offsetYStep = state.canvasSize.height / steps
            val price = minPrice + ((steps - step) * priceStep)
            val text = TextLine.make(price.priceRound(maxPrice - minPrice).toInt().toString(), TextRendering.fontAxis)
            val topOffset = step * offsetYStep
            if (topOffset < text.height) return@PriceAxisContentTemplate
            nativeCanvas.drawTextLine(
                text,
                canvasSize.width - text.width - PriceDashboardSizes.AxisPadding.toPx(),
                topOffset + text.descent,
                TextRendering.paint
            )
        })
}

@Composable
private fun PriceAxisGrid(items: List<PriceItem>, state: PriceBoardState) {
    val density = LocalDensity.current.density
    val canvasSize = state.canvasSize
    val steps = state.verticalSteps()
    val offsetYStep = state.canvasSize.height / steps

    PriceAxisContentTemplate(items, state,
        horizontalContent = { priceItem, _ ->
            val bottomAxisHeight = state.bottomAxisBarHeight(density)
            drawLine(PriceDashboardColor.GridLine, start = Offset(0f, -canvasSize.height), end = Offset(0f, -bottomAxisHeight))
        },
        verticalContent = { step, _ ->
            val topOffset = step * offsetYStep
            drawLine(PriceDashboardColor.GridLine, start = Offset(0f, topOffset), end = Offset(canvasSize.width, topOffset))
        })
}

@Composable
private fun PriceAxisContentTemplate(
    items: List<PriceItem>,
    state: PriceBoardState,
    horizontalContent: DrawScope.(PriceItem?, step: Int) -> Unit,
    verticalContent: DrawScope.(step: Int, steps: Int) -> Unit
) {
    val density = LocalDensity.current.density
    Canvas {
        //Axis X
        clipRect(right = state.verticalPriceBarLeft(density)) {
            translate(state.offset.x, size.height) {
                scale(state.scale.x, 1f, pivot = state.chartScaleOffset()) {
                    val step = ceil(TextRendering.axisXStep / state.scale.x).toInt()
                    val viewportIndexes = state.viewportIndexes()
                    viewportIndexes.forEach { i ->
                        //can't use step on range as it's causing scroll "jitter"
                        if (i % step != 0) return@forEach
                        val x = (i + 0.5f) * PriceDashboardSizes.PriceItemWidth
                        translate(x, 0f) {
                            scale(scaleX = 1f / state.scale.x, scaleY = 1f, pivot = Offset.Zero) {
                                horizontalContent(items.getOrNull(i), step)
                            }
                        }
                    }
                }
            }
        }

        //Axis Y
        clipRect(bottom = size.height - state.bottomAxisBarHeight(density)) {
            val steps = state.verticalSteps()
            (0 until steps).forEach { step ->
                verticalContent(step, steps)
            }
        }
    }
}

@Composable
private fun PriceBoardMouse(items: List<PriceItem>, state: PriceBoardState) {
    val metrics = remember { TextRendering.fontAxis.metrics }
    val density = LocalDensity.current.density
    val effect = remember { PathEffect.dashPathEffect(floatArrayOf(10f * density, 10f * density)) }
    val bottomAxisBarHeight = state.bottomAxisBarHeight(density, TextRendering.fontAxis.metrics)
    val verticalPriceBarLeft = state.verticalPriceBarLeft(density)

    Canvas {
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


        //vertical
        if (state.pointer.x <= verticalPriceBarLeft) {
            drawLine(
                PriceDashboardColor.MouseCross,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = PriceDashboardSizes.MouseCrossStrokeWidth.toPx(),
                pathEffect = effect
            )

            val label = state.horizontalLabel(items)
            if (label != null) {
                val textPadding = 8.dp.toPx()
                val text = TextLine.make(label, TextRendering.fontLabels)
                val textBoxSize = text.size(horizontalPadding = textPadding).let { it.copy(height = max(it.height, bottomAxisBarHeight)) }
                val top = state.canvasSize.height - textBoxSize.height
                translate(x - textBoxSize.width / 2, top) {
                    drawRect(PriceDashboardColor.BackgroundPriceBubble, size = textBoxSize)
                    nativeCanvas.drawTextLine(text, textPadding, (-text.descent + textBoxSize.height + text.height) / 2f, TextRendering.paint)
                }
            }
        }

        //horizontal
        if (state.canvasSize.height - state.pointer.y > bottomAxisBarHeight) {
            drawLine(
                PriceDashboardColor.MouseCross,
                start = Offset(0f, state.pointer.y),
                end = Offset(size.width, state.pointer.y),
                strokeWidth = PriceDashboardSizes.MouseCrossStrokeWidth.toPx(),
                pathEffect = effect
            )

            val text = TextLine.make(state.verticalLabel(), TextRendering.fontAxis)
            val textPadding = PriceDashboardSizes.AxisPadding.toPx()
            val textSize = text.size(textPadding)
            val top = state.pointer.y - textSize.height / 2
            val left = state.verticalPriceBarLeft(density)
            val verticalAxisBarWidth = PriceDashboardSizes.VerticalPriceBarWidth.toPx(density)
            translate(left, top) {
                drawRect(PriceDashboardColor.BackgroundPriceBubble, size = Size(verticalAxisBarWidth, textSize.height))
                nativeCanvas.drawTextLine(text, verticalAxisBarWidth - text.width - textPadding, textPadding - text.ascent, TextRendering.paint)
            }
        }
    }
}

@Composable
private fun PriceBoardDebug(items: List<PriceItem>, state: PriceBoardState) {
    Canvas {
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
            "Mouse: Index=${state.selectedPriceItemIndex()}, Price:${state.mousePrice()}",
        )
        drawIntoCanvas {
            translate(left = 2f, top = 60.dp.toPx()) {
                rows.forEachIndexed { index, s ->
                    it.nativeCanvas.drawTextLine(TextLine.Companion.make(s, TextRendering.font), 0f, index * TextRendering.font.metrics.height, TextRendering.paint)
                }
            }
        }
    }
}

@Composable
fun Canvas(modifier: Modifier = Modifier, content: DrawScope.() -> Unit) {
    Spacer(modifier = modifier.fillMaxSize()
        .drawBehind {
            clipRect {
                content()
            }
        })
}