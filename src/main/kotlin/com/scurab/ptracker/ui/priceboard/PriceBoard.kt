@file:OptIn(ExperimentalComposeUiApi::class)

package com.scurab.ptracker.ui.priceboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.ext.clipRectSafe
import com.scurab.ptracker.ext.f
import com.scurab.ptracker.ext.f3
import com.scurab.ptracker.ext.filterVisible
import com.scurab.ptracker.ext.getHorizontalAxisText
import com.scurab.ptracker.ext.getLabelPriceDecimals
import com.scurab.ptracker.ext.nHeight
import com.scurab.ptracker.ext.nWidth
import com.scurab.ptracker.ext.nativeCanvas
import com.scurab.ptracker.ext.normalize
import com.scurab.ptracker.ext.toLabelPrice
import com.scurab.ptracker.ext.resetScale
import com.scurab.ptracker.ext.size
import com.scurab.ptracker.ext.toLTRBWH
import com.scurab.ptracker.ext.transformNormToReal
import com.scurab.ptracker.ext.transformNormToViewPort
import com.scurab.ptracker.ext.withTranslateAndScale
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.priceDetails
import com.scurab.ptracker.model.randomPriceData
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppTheme.DashboardColors
import com.scurab.ptracker.ui.AppTheme.DashboardSizes
import com.scurab.ptracker.ui.AppTheme.TextRendering
import com.scurab.ptracker.ui.common.VerticalDivider
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextLine
import java.lang.Float.min
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

object PriceDashboardConfig {
    val ScaleRangeX = floatArrayOf(1e-2f, 20f)
    val ScaleRangeY = floatArrayOf(1e-6f, 1e7f)
    const val Debug = true
    const val SnappingMouseCrossHorizontally = true
    const val AxisYContentCoef = 0.5f
    const val DefaultMinColumns = 100
    const val AxisXStep = 5
}

private fun PriceBoardState.columns() = (viewport().nWidth / DashboardSizes.PriceItemWidth).roundToInt()
private fun PriceBoardState.horizontalLabel(items: List<PriceItem>): String? = items.getOrNull(selectedPriceItemIndex())?.fullDate
private fun PriceBoardState.mousePrice() = normalizedPointer().transformNormToViewPort(viewport()).y
private fun PriceBoardState.verticalLabel(): String = mousePrice().f(visiblePriceRange().getLabelPriceDecimals())
private fun PriceBoardState.verticalSteps() = (floor(canvasSize.height / TextRendering.font.metrics.height).toInt() * PriceDashboardConfig.AxisYContentCoef).toInt()
private fun PriceBoardState.viewportColumnWidth() = DashboardSizes.PriceItemWidth * scale.x
private fun PriceBoardState.viewportIndexes(startOffset: Int = 0, endOffset: Int = 0): IntProgression {
    val vp = viewport()
    val colWidth = DashboardSizes.PriceItemWidth
    val firstIndex = floor(vp.left / colWidth).toInt()
    val lastIndex = firstIndex + ceil(vp.nWidth / colWidth).toInt()
    return (firstIndex + startOffset) until (lastIndex + endOffset)
}

fun PriceItem.isVisible(state: PriceBoardState, viewport: Rect = state.viewport()): Boolean {
    val colWidth = DashboardSizes.PriceItemWidth
    val firstIndex = (max(0f, viewport.left) / colWidth).toInt()
    val widthToFill = viewport.nWidth + min(viewport.left, 0f)
    val lastIndex = firstIndex + widthToFill
    //TODO: add vertically
    return firstIndex <= index && index <= lastIndex
}

@Composable
fun PriceBoard(vm: PriceBoardViewModel) {
    val uiState by vm.uiState.collectAsState()
    when (val uiState = uiState) {
        is PriceBoardUiState.NoAssetSelected -> Text("Select Asset")
        is PriceBoardUiState.Data -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.current.BackgroundContent)
            ) {
                Row {
                    Box(modifier = Modifier.weight(1f)) {
                        PriceBoard(uiState.priceBoardState)
                    }
                    VerticalDivider()
                    Column {
                        uiState.pairs.forEach { pair ->
                            Button(onClick = { vm.onPairSelected(pair) }) {
                                Text(pair)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceBoard(state: PriceBoardState) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerHoverIcon(state.mouseIcon)
            .background(AppColors.current.BackgroundContent)
            .onSizeChange(state)
            .onMouseMove(state)
            .onMouseDrag(state)
            .onWheelScroll(state)
    ) {
        Grid(state)
        Candles(state)
        AxisBackground(state)
        AxisContent(state)
        Mouse(state)
        AxisEdgeLines(state)
        if (PriceDashboardConfig.Debug) {
            PriceBoardDebug(state)
        }
        Column(modifier = Modifier.align(Alignment.TopEnd).offset(x = (-130).dp)) {
            Button(onClick = { scope.launch { state.reset() } }) {
                Text("R")
            }
            Button(onClick = { scope.launch { state.setViewport(state.initViewport(state.canvasSize), animate = true) } }) {
                Text("I")
            }
            Button(onClick = {
                scope.launch {
                    state.items = randomPriceData(Random, Random.nextInt(500, 1000), Clock.System.now().minus(1000L.days).toLocalDateTime(TimeZone.UTC), 1L.days)
                    state.setViewport(state.initViewport(state.canvasSize), animate = true)
                }
            }
            ) {
                Text("D")
            }
        }

        PriceSelectedDayDetail(state)
        LaunchedEffect(state.animateInitViewPort) {
            scope.launch {
                state.setViewport(state.initViewport(state.canvasSize), animate = true)
            }
        }
    }
}

@Composable
private fun PriceSelectedDayDetail(state: PriceBoardState) {
    val item = state.pointedPriceItem
    if (item != null) {
        val text = remember(item) { item.priceDetails() }
        Text(
            text, modifier = Modifier
                .offset(8.dp, 4.dp)
                .background(DashboardColors.BackgroundAxis, shape = RoundedCornerShape(2.dp))
                .padding(2.dp),
            color = DashboardColors.OnBackground,
            fontSize = DashboardSizes.PriceSelectedDayDetail,
            fontFamily = FontFamily.Monospace
        )
    }
}


@Composable
private fun Candles(state: PriceBoardState) {
    val priceItemWidthHalf = DashboardSizes.PriceItemWidth / 2f
    Canvas {
        withTranslateAndScale(state) {
            state.items.filterVisible(state, endOffset = 1).forEach { priceItem ->
                val x = priceItem.index * DashboardSizes.PriceItemWidth
                //scaleY flipped as we want to have origin at left/Bottom
                translate(x, 0f) {
                    drawRect(priceItem.color, topLeft = Offset(0f, priceItem.rectOffsetY), size = priceItem.rectSize)
                    drawLine(
                        priceItem.color,
                        start = Offset(priceItemWidthHalf, priceItem.spikeOffsetY1),
                        end = Offset(priceItemWidthHalf, priceItem.spikeOffsetY2),
                        //keep the strokeWidth scale independent
                        strokeWidth = DashboardSizes.SpikeLineStrokeWidth.toPx() / state.scale.x
                    )
                    if (PriceDashboardConfig.Debug) {
                        translate(priceItemWidthHalf, priceItem.centerY) {
                            resetScale(state) {
//                                drawCircle(
//                                    Color.Yellow.copy(alpha = 0.5f),
//                                    radius = priceItemWidthHalf,
//                                    center = Offset.Zero
//                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AxisBackground(state: PriceBoardState) {
    val bottomAxisHeight = state.bottomAxisBarHeight()
    val canvasSize = state.canvasSize
    val verticalPriceBarLeft = state.verticalPriceBarLeft()
    val axisBackgroundPath = remember(canvasSize, verticalPriceBarLeft) {
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
        drawPath(axisBackgroundPath, DashboardColors.BackgroundAxis)
    }
}

@Composable
private fun AxisEdgeLines(state: PriceBoardState) {
    val bottomAxisHeight = state.bottomAxisBarHeight()
    val canvasSize = state.canvasSize
    val verticalPriceBarLeft = state.verticalPriceBarLeft()
    Canvas {
        drawLine(
            DashboardColors.BackgroundAxisEdge,
            start = Offset(0f, canvasSize.height - bottomAxisHeight),
            end = Offset(verticalPriceBarLeft, canvasSize.height - bottomAxisHeight),
            strokeWidth = DashboardSizes.GridLineStrokeWidth.toPx()
        )
        drawLine(
            DashboardColors.BackgroundAxisEdge,
            start = Offset(verticalPriceBarLeft, 0f),
            end = Offset(verticalPriceBarLeft, canvasSize.height - bottomAxisHeight),
            strokeWidth = DashboardSizes.GridLineStrokeWidth.toPx()
        )
    }
}


@Composable
private fun AxisContent(state: PriceBoardState) {
    val items = state.items
    val metrics = remember { TextRendering.fontAxis.metrics }
    val canvasSize = state.canvasSize
    val viewport = state.viewport()

    PriceAxisContentTemplate(items, state,
        horizontalContent = { priceItem, step ->
            priceItem ?: return@PriceAxisContentTemplate
            val bottomAxisHeight = state.bottomAxisBarHeight()
            val label = items.getHorizontalAxisText(priceItem.index, step)
            val text = TextLine.make(label, TextRendering.fontAxis)
            nativeCanvas.drawTextLine(text, -text.width / 2, (-bottomAxisHeight + text.height - metrics.bottom) / 2, TextRendering.paint)
        },
        verticalContent = { step, steps ->
            val minPrice = viewport.bottom
            val maxPrice = minPrice + viewport.nHeight
            val priceStep = (maxPrice - minPrice) / steps.toFloat()
            val offsetYStep = state.canvasSize.height / steps
            val price = minPrice + ((steps - step) * priceStep)
            val text = TextLine.make(price.toLabelPrice(minPrice.rangeTo(maxPrice)), TextRendering.fontAxis)
            val topOffset = step * offsetYStep
            if (topOffset < text.height) return@PriceAxisContentTemplate
            nativeCanvas.drawTextLine(
                text,
                canvasSize.width - text.width - DashboardSizes.VerticalAxisHorizontalPadding.toPx(),
                topOffset + text.descent,
                TextRendering.paint
            )
        })
}

@Composable
private fun Grid(state: PriceBoardState) {
    val items = state.items
    val canvasSize = state.canvasSize
    val steps = state.verticalSteps()
    val offsetYStep = state.canvasSize.height / steps

    PriceAxisContentTemplate(items, state,
        horizontalContent = { priceItem, _ ->
            val bottomAxisHeight = state.bottomAxisBarHeight()
            drawLine(DashboardColors.GridLine, start = Offset(0f, -canvasSize.height), end = Offset(0f, -bottomAxisHeight))
        },
        verticalContent = { step, _ ->
            val topOffset = step * offsetYStep
            drawLine(DashboardColors.GridLine, start = Offset(0f, topOffset), end = Offset(canvasSize.width, topOffset))
        })
}

@Composable
private fun PriceAxisContentTemplate(
    items: List<PriceItem>,
    state: PriceBoardState,
    horizontalContent: DrawScope.(PriceItem?, step: Int) -> Unit,
    verticalContent: DrawScope.(step: Int, steps: Int) -> Unit
) {
    Canvas {
        //Axis X
        clipRectSafe(right = state.verticalPriceBarLeft()) {
            translate(-state.offset.x, size.height) {
                scale(state.scale.x, 1f, pivot = state.chartScalePivot()) {
                    val step = ceil(PriceDashboardConfig.AxisXStep * state.maxDensity() / state.scale.x).toInt()
                    //offset for long text to be visible even if line the "column" is outside visible range
                    val viewportIndexes = state.viewportIndexes(startOffset = -step)
                    viewportIndexes.forEach { i ->
                        //can't use step on range as it's causing scroll "jitter"
                        if (i % step != 0) return@forEach
                        val x = (i + 0.5f) * DashboardSizes.PriceItemWidth
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
        clipRectSafe(bottom = size.height - state.bottomAxisBarHeight()) {
            val steps = state.verticalSteps()
            (0 until steps).forEach { step ->
                verticalContent(step, steps)
            }
        }
    }
}

@Composable
private fun Mouse(state: PriceBoardState) {
    if (state.pointer.isEmpty) return
    val items = state.items
    val density = LocalDensity.current.density
    val effect = remember { PathEffect.dashPathEffect(floatArrayOf(10f * density, 10f * density)) }
    val bottomAxisBarHeight = state.bottomAxisBarHeight(TextRendering.fontAxis.metrics)
    val verticalPriceBarLeft = state.verticalPriceBarLeft()

    Canvas {
        val colWidth = DashboardSizes.PriceItemWidth
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
                DashboardColors.MouseCross,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = DashboardSizes.MouseCrossStrokeWidth.toPx(),
                pathEffect = effect
            )

            val label = state.horizontalLabel(items)
            if (label != null) {
                val textPadding = 8.dp.toPx()
                val text = TextLine.make(label, TextRendering.fontLabels)
                val textBoxSize = text.size(horizontalPadding = textPadding).let { it.copy(height = max(it.height, bottomAxisBarHeight)) }
                val top = state.canvasSize.height - textBoxSize.height
                translate(x - textBoxSize.width / 2, top) {
                    drawRect(DashboardColors.BackgroundPriceBubble, size = textBoxSize)
                    nativeCanvas.drawTextLine(text, textPadding, (-text.descent + textBoxSize.height + text.height) / 2f, TextRendering.paint)
                }
            }
        }

        //horizontal
        if (state.canvasSize.height - state.pointer.y > bottomAxisBarHeight) {
            drawLine(
                DashboardColors.MouseCross,
                start = Offset(0f, state.pointer.y),
                end = Offset(size.width, state.pointer.y),
                strokeWidth = DashboardSizes.MouseCrossStrokeWidth.toPx(),
                pathEffect = effect
            )

            val text = TextLine.make(state.verticalLabel(), TextRendering.fontAxis)
            val textPadding = DashboardSizes.VerticalAxisHorizontalPadding.toPx()
            val textSize = text.size(textPadding)
            val top = state.pointer.y - textSize.height / 2
            val verticalAxisBarWidth = state.verticalPriceBarWidth()
            translate(verticalPriceBarLeft, top) {
                drawRect(DashboardColors.BackgroundPriceBubble, size = Size(verticalAxisBarWidth, textSize.height))
                nativeCanvas.drawTextLine(text, verticalAxisBarWidth - text.width - textPadding, textPadding - text.ascent, TextRendering.paint)
            }
        }
    }
}

@Composable
private fun PriceBoardDebug(state: PriceBoardState) {
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
            "Data: Items:${state.items.size}, LastItemPriceCenter:${state.items.lastOrNull()?.centerY?.f3}",
        )
        drawIntoCanvas {
            translate(left = 2f, top = 60.dp.toPx()) {
                rows.forEachIndexed { index, s ->
                    it.nativeCanvas.drawTextLine(TextLine.make(s, TextRendering.font), 0f, index * TextRendering.font.metrics.height, TextRendering.paint)
                }
            }
        }

        drawLine(Color.Magenta, start = Offset(0f, canvasSize.height / 2), end = Offset(canvasSize.width, canvasSize.height / 2))
        drawLine(Color.Magenta, start = Offset(canvasSize.width / 2, 0f), end = Offset(canvasSize.width / 2, canvasSize.height))

        withTranslateAndScale(state) {
            val size = 100f
            drawLine(Color.Green, start = Offset(0f, -size / state.scale.y), end = Offset(0f, size / state.scale.y))
            drawLine(Color.Green, start = Offset(-size / state.scale.x, 0f), end = Offset(size / state.scale.x, 0f))
        }
    }
}

@Composable
private fun Canvas(modifier: Modifier = Modifier, content: DrawScope.() -> Unit) {
    Spacer(modifier = modifier.fillMaxSize()
        .drawBehind {
            clipRectSafe {
                content()
            }
        })
}

