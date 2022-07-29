package com.scurab.ptracker.ui.priceboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.scurab.ptracker.app.ext.clipRectSafe
import com.scurab.ptracker.app.ext.draw
import com.scurab.ptracker.app.ext.f
import com.scurab.ptracker.app.ext.f3
import com.scurab.ptracker.app.ext.getHorizontalAxisText
import com.scurab.ptracker.app.ext.getLabelPriceDecimals
import com.scurab.ptracker.app.ext.hrs
import com.scurab.ptracker.app.ext.isNotNullAndNotZero
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.isPositive
import com.scurab.ptracker.app.ext.isZeroOrPositive
import com.scurab.ptracker.app.ext.maxValue
import com.scurab.ptracker.app.ext.nHeight
import com.scurab.ptracker.app.ext.nWidth
import com.scurab.ptracker.app.ext.nativeCanvas
import com.scurab.ptracker.app.ext.normalize
import com.scurab.ptracker.app.ext.resetScale
import com.scurab.ptracker.app.ext.size
import com.scurab.ptracker.app.ext.toLTRBWH
import com.scurab.ptracker.app.ext.toLabelPrice
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.ext.transformNormToReal
import com.scurab.ptracker.app.ext.transformNormToViewPort
import com.scurab.ptracker.app.ext.withTranslateAndScale
import com.scurab.ptracker.app.ext.withTranslateAndScaleDefaults
import com.scurab.ptracker.app.ext.withTranslateAndScaleY
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Filter
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.model.priceDetails
import com.scurab.ptracker.component.compose.StateContainer
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.AppTheme.DashboardColors
import com.scurab.ptracker.ui.AppTheme.DashboardSizes
import com.scurab.ptracker.ui.AppTheme.TextRendering
import com.scurab.ptracker.ui.common.AssetToggleButton
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.FlatButton
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.ToggleButton
import com.scurab.ptracker.ui.common.TransactionRow
import com.scurab.ptracker.ui.common.VerticalDivider
import com.scurab.ptracker.ui.model.IconColor
import org.jetbrains.skia.Point
import org.jetbrains.skia.TextLine
import java.lang.Float.min
import java.math.BigDecimal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

object PriceDashboardConfig {
    val ScaleRangeX = floatArrayOf(1e-2f, 20f)
    val ScaleRangeY = floatArrayOf(1e-6f, 1e7f)
    const val SnappingMouseCrossHorizontally = true
    const val AxisYContentCoef = 0.5f
    const val DefaultMinColumns = 200
    const val AxisXStep = 5
    const val ViewportAnimationDuration = 300
    const val FocusInitRadius = 100f
    const val FocusAnimationDuration = 300
}

private fun PriceBoardState.columns() = (viewport().nWidth / DashboardSizes.PriceItemWidth).roundToInt()
private fun PriceBoardState.horizontalLabel(items: List<PriceItem>): String? = items.getOrNull(selectedPriceItemIndex())?.formattedFullDate
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
    val lastIndex = firstIndex + (widthToFill / colWidth)
    return firstIndex <= index && index <= lastIndex && viewport.bottom <= centerY && centerY <= viewport.top
}

@Composable
fun PriceBoardScreen(vm: PriceBoardViewModel) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        vm.uiState.priceBoardState.init(coroutineScope)
    }
    val uiState = vm.uiState
    val priceBoardState = uiState.priceBoardState
    Box(
        modifier = Modifier.fillMaxSize().background(AppColors.current.BackgroundContent)
    ) {
        Column {
            Box(modifier = Modifier.zIndex(1f)) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier.width(IntrinsicSize.Max).height(IntrinsicSize.Min).horizontalScroll(scrollState)
                ) {
                    FlatButton(Icons.Default.BorderOuter, onClick = { vm.onResetClicked() })
                    VerticalDivider()
                    ToggleButton(Icons.Default.FilterAlt, isSelected = uiState.hasTradeOnlyFilter, onClick = { vm.onFilterClicked(Filter.ImportantTransactions) })
                    VerticalDivider()
                    ToggleButton(Icons.Default.Equalizer, isSelected = priceBoardState.isTradingVolumeVisible, onClick = { vm.onTradingVolumeClicked() })
                    VerticalDivider()
                    ToggleButton(Icons.Default.Menu, isSelected = priceBoardState.isTradingAverageVisible, onClick = { vm.onTradingAverageClicked() })
                    VerticalDivider()
                    ToggleButton(Icons.Default.Hive, isSelected = priceBoardState.isGroupingTransactionsEnabled, onClick = { vm.onGroupingTransactionsClicked() })
                    VerticalDivider()
                    val assets = vm.uiState.assets
                    assets.forEach { assetIcon ->
                        val isSelected = assetIcon.asset == vm.uiState.priceBoardState.selectedAsset
                        AssetToggleButton(assetIcon.asset, uiState.prices[assetIcon.asset], isSelected = isSelected, onClick = { vm.onAssetSelected(assetIcon.asset) })
                        VerticalDivider()
                    }
                }
                HorizontalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState), modifier = Modifier.align(Alignment.BottomCenter).offset(0.dp, LocalScrollbarStyle.current.thickness / 2f)
                )
            }
            if (priceBoardState.priceItems.isNotEmpty()) {
                Row(modifier = Modifier.zIndex(0f)) {
                    Box(modifier = Modifier.weight(1f)) {
                        PriceBoardScreen(priceBoardState, vm)
                    }
                    val scale = LocalDensity.current.maxValue()
                    Column(modifier = Modifier.width(width = 220.dp * scale)) {
                        PriceBoardTransactions(priceBoardState, vm)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceBoardTransactions(priceBoardState: PriceBoardState, eventDelegate: PriceBoardEventDelegate) {
    val selectedAsset = priceBoardState.selectedAsset
    if (selectedAsset != null) {
        val state = remember(selectedAsset) { LazyListState(0, 0) }
        LaunchedEffect(selectedAsset, priceBoardState.scrollToTransactionIndex) {
            state.animateScrollToItem(priceBoardState.scrollToTransactionIndex)
        }
        Box {
            val priceItem = priceBoardState.pointedPriceItem
            val grouping = priceBoardState.grouping::toLongGroup
            LazyColumn(modifier = Modifier.fillMaxWidth(), state = state) {
                itemsIndexed(priceBoardState.transactions, key = { _, v -> v.uuid }) { index, transaction ->
                    val isSelected = priceItem != null && grouping(priceItem.dateTime) == grouping(transaction.dateTime)
                    TransactionRow(
                        onClick = {
                            priceBoardState.clickedTransaction = System.currentTimeMillis() to transaction
                            eventDelegate.onTransactionClicked(transaction, it)
                        },
                        onHoverChange = { eventDelegate.onTransactionHoverChanged(transaction, it) },
                        index, transaction, isSelected,
                    )
                    Divider()
                }
            }
            VerticalScrollbar(adapter = rememberScrollbarAdapter(state), modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun PriceBoardScreen(state: PriceBoardState, eventDelegate: PriceBoardEventDelegate) {
    Box(
        modifier = Modifier.fillMaxSize().pointerHoverIcon(state.mouseIcon).background(AppColors.current.BackgroundContent).onSizeChange(state).onMouseMove(state)
            .onMouseDrag(state)
            //disabled, doesn't work properly with dragDetection
            //.onDoubleTap(state)
            .onWheelScroll(state)
    ) {
        Grid(state)
        Candles(state)
        CandleTransactions(state)
        CandleVolumes(state)
        AxisBackground(state)
        AxisContent(state)
        AvgVisiblePrices(state)
        Mouse(state)
        AxisEdgeLines(state)
        if (state.isDebugVisible) {
            PriceBoardDebug(state)
        }

        PriceDetails(state)
        PriceSelectedDayTransactionTypes(state)
        if (state.animateInitViewPort != -1L) {
            LaunchedEffect(state.animateInitViewPort) {
                state.setViewport(state.initViewport(), animate = true)
            }
        }
    }
}

@Composable
private fun PriceDetails(state: PriceBoardState) {
    val item = state.pointedPriceItem
    val viewPort = state.viewport()
    val stats = state.getVisibleStats(viewPort)
    val sizes = AppSizes.current
    Column(
        modifier = Modifier
            .offset(sizes.Space, sizes.Space)
            .background(DashboardColors.BackgroundAxis, shape = RoundedCornerShape(2.dp))
            .padding(horizontal = sizes.Space2, vertical = sizes.Space)
    ) {
        if (item != null) {
            val text = remember(item) { item.priceDetails() }
            StatsText(text)
            HSpacer()
        }

        if (!stats.isEmpty && state.isTradingAverageVisible) {
            val marketColor = AppColors.current.Secondary
            val label = remember(stats) {
                textTradingAverages(stats.avgMarketPrice, stats.avgCoin1BuyPrice, stats.avgCoin1SellPrice, market = marketColor, DashboardColors.Candle)
            }
            StatsText(label)
            HSpacer()
        }

        if (!stats.isEmpty && state.isTradingVolumeVisible) {
            val text = remember(stats, state.selectedPriceItem()) {
                val dayVolumes = stats.volumes[state.selectedPriceItem()]
                textVolumeStats(
                    stats.asset, stats.coin1Sum, stats.coin2Sum,
                    dayVolumes?.coin1Volume, dayVolumes?.coin2Volume,
                    DashboardColors.Candle
                )
            }
            if (text.isNotEmpty()) {
                StatsText(text)
            }
        }
    }
}

@Composable
private fun StatsText(text: AnnotatedString) {
    Text(text, color = DashboardColors.OnBackground, fontSize = DashboardSizes.PriceSelectedDayDetail, fontFamily = FontFamily.Monospace, maxLines = 1)
}

@Composable
private fun BoxScope.PriceSelectedDayTransactionTypes(state: PriceBoardState) {
    val item = state.pointedPriceItem
    if (item != null) {
        val iconsPrices = state.transactionsPerPriceItem[item.dateTime]?.distinctIcons ?: return

        val offsetX = AppSizes.current.Space + Dp(state.verticalPriceBarWidth() / LocalDensity.current.density)
        if (iconsPrices.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd).offset(-offsetX, AppSizes.current.Space).background(DashboardColors.BackgroundAxis, AppTheme.Shapes.RoundedCornersSize2)
                    .padding(AppSizes.current.Space2)
            ) {
                iconsPrices.forEach { ic ->
                    Icon(imageVector = ic.imageVector.get(), contentDescription = "", tint = ic.color.get())
                }
            }
        }
    }
}

@Composable
private fun Candles(state: PriceBoardState) {
    val priceItemWidthHalf = DashboardSizes.PriceItemWidth / 2f
    val visiblePriceItems = state.getVisiblePriceItems()
    Canvas {
        withTranslateAndScale(state) {
            visiblePriceItems.forEach { priceItem ->
                val x = priceItem.index * DashboardSizes.PriceItemWidth
                //scaleY flipped as we want to have origin at left/Bottom
                translate(x, 0f) {
                    drawRect(priceItem.color, topLeft = Offset(0f, priceItem.rectOffsetY), size = priceItem.rectSize)
                    drawLine(
                        priceItem.color, start = Offset(priceItemWidthHalf, priceItem.spikeOffsetY1), end = Offset(priceItemWidthHalf, priceItem.spikeOffsetY2),
                        //keep the strokeWidth scale independent
                        strokeWidth = DashboardSizes.SpikeLineStrokeWidth.toPx() / state.scale.x
                    )
                }
            }
        }
    }
}

@Composable
private fun CandleTransactions(state: PriceBoardState) {
    state.selectedAsset ?: return
    val densityScale = LocalDensity.current.maxValue()
    val priceItemWidthHalf = DashboardSizes.PriceItemWidth / 2f

    state.getVisiblePriceItems().forEach { priceItem ->
        val iconsPrices = state.transactionsPerPriceItem[priceItem.dateTime]?.iconPrices
        //draw candle transaction
        var pointedIconPrice: Pair<IconColor, Transaction>? = null
        iconsPrices?.forEach iconPrices@{ iconPrice ->
            val (ic, transaction) = iconPrice
            val painter = rememberVectorPainter(image = ic.imageVector.get())
            if (state.pointingTransaction == transaction) {
                pointedIconPrice = iconPrice
                return@iconPrices
            }
            if (!state.isGroupingTransactionsEnabled) {
                drawCandleTransactionIcon(state, priceItem, transaction, priceItemWidthHalf, ic, densityScale, painter)
            }
        }
        if (iconsPrices?.isNotEmpty() == true && state.isGroupingTransactionsEnabled) {
            val ic = AppTheme.TransactionIcons.IconsMap.getValue(Transaction._TypeGrouping)
            val painter = rememberVectorPainter(image = ic.imageVector.get())
            drawCandleTransactionIcon(state, priceItem, transaction = null, priceItemWidthHalf, ic, densityScale, painter)
        }
        //draw the pointing one latest to overdraw any other
        pointedIconPrice?.let { (ic, transaction) ->
            val painter = rememberVectorPainter(image = ic.imageVector.get())
            drawCandleTransactionIcon(state, priceItem, transaction, priceItemWidthHalf, ic, densityScale, painter)
        }
    }

    //transaction highlight
    state.clickedTransaction?.let { (timestamp, transaction) ->
        transaction.priceItem?.let { priceItem ->
            val radius = remember(timestamp, transaction) { Animatable(PriceDashboardConfig.FocusInitRadius) }
            LaunchedEffect(timestamp, transaction) {
                radius.animateTo(0f, tween(delayMillis = PriceDashboardConfig.ViewportAnimationDuration, durationMillis = PriceDashboardConfig.FocusAnimationDuration))
            }
            val density = LocalDensity.current.density
            val drawStyle = remember { Stroke(width = 8.dp.toPx(density)) }
            Canvas {
                withTranslateAndScale(state) {
                    val x = priceItem.index * DashboardSizes.PriceItemWidth
                    //scaleY flipped as we want to have origin at left/Bottom
                    val y = transaction.unitPrice()?.toFloat() ?: priceItem.centerY
                    translate(x + priceItemWidthHalf, y) {
                        resetScale(state) {
                            drawCircle(AppTheme.Colors.Secondary, radius = radius.value, center = Offset.Zero, style = drawStyle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CandleVolumes(state: PriceBoardState) {
    if (!state.isTradingVolumeVisible) return
    state.selectedAsset ?: return
    val visibleVolumes = state.getVisibleStats()
    Canvas {
        val scaleY1 = size.height * DashboardSizes.VolumeMaxWindowHeight / visibleVolumes.coin1VolumeAbsMax.abs().toFloat()
        val hairLine = DashboardSizes.GridLineStrokeWidth.toPx() / state.scale.x
        withTranslateAndScaleDefaults(
            state, translateTop = state.canvasSize.height - state.bottomAxisBarHeight(), scaleY = 1f
        ) {
            visibleVolumes.volumes
                .asSequence()
                .mapNotNull { it.value }
                .forEach { (priceItem, vol1, _) ->
                    val color1 = DashboardColors.CandleVolume.default2If(/*greenIf*/vol1.isPositive)
                    val x = priceItem.index * DashboardSizes.PriceItemWidth
                    val v1 = vol1.abs().toFloat()
                    if (v1 > 0f) {
                        translate(x, 0f) {
                            val height = v1 * scaleY1
                            drawRect(
                                color1, topLeft = Offset(hairLine, 0f), size = Size((DashboardSizes.PriceItemWidth - hairLine).coerceAtLeast(hairLine), height)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun AvgVisiblePrices(state: PriceBoardState) {
    if (!state.isTradingAverageVisible) return
    state.selectedAsset ?: return
    val averagePrices = state.getVisibleStats().takeIf { it.avgMarketPrice.isNotZero() } ?: return

    val density = LocalDensity.current.density
    val lineWidth = state.verticalPriceBarLeft()
    val effect = remember { PathEffect.dashPathEffect(floatArrayOf(3f * density, 3f * density)) }
    val marketPriceColor = AppColors.current.Secondary
    val tradeColor = AppColors.current.Green
    Canvas {
        withTranslateAndScaleY(state) {
            val avgMarketPrice = averagePrices.avgMarketPrice
            val avgBuyPrice = averagePrices.avgCoin1BuyPrice
            drawLine(
                marketPriceColor, Offset(0f, avgMarketPrice.toFloat()), end = Offset(lineWidth, avgMarketPrice.toFloat()), pathEffect = effect
            )
            if (avgBuyPrice.isNotZero()) {
                drawLine(
                    tradeColor, Offset(0f, avgBuyPrice.toFloat()), end = Offset(lineWidth, avgBuyPrice.toFloat()), pathEffect = effect
                )
            }
        }
    }
}

@Composable
private fun drawCandleTransactionIcon(
    state: PriceBoardState, priceItem: PriceItem, transaction: Transaction?, priceItemWidthHalf: Float, ic: IconColor, densityScale: Float, painter: VectorPainter
) {
    Canvas {
        withTranslateAndScale(state) {
            val x = priceItem.index * DashboardSizes.PriceItemWidth
            val y = transaction?.unitPrice()?.toFloat() ?: priceItem.centerY
            translate(x + priceItemWidthHalf, y) {
                resetScale(state) {
                    translate(ic.candleOffset.x, ic.candleOffset.y) {
                        val scale = ic.candleScale.get(isSelected = state.pointingTransaction == transaction) * densityScale
                        draw(painter, scale, colorFilter = tint(color = ic.color.get(isSelected = state.clickedTransaction == transaction)))
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
    val items = state.priceItems
    val metrics = remember { TextRendering.fontAxis.metrics }
    val canvasSize = state.canvasSize
    val viewport = state.viewport()

    PriceAxisContentTemplate(items, state, horizontalContent = { priceItem, step ->
        priceItem ?: return@PriceAxisContentTemplate
        val bottomAxisHeight = state.bottomAxisBarHeight()
        val label = items.getHorizontalAxisText(priceItem.index, step)
        val text = TextLine.make(label, TextRendering.fontAxis)
        nativeCanvas.drawTextLine(text, -text.width / 2, (-bottomAxisHeight + text.height - metrics.bottom) / 2, TextRendering.paint)
    }, verticalContent = { step, steps ->
        val minPrice = viewport.bottom
        val maxPrice = minPrice + viewport.nHeight
        val priceStep = (maxPrice - minPrice) / steps.toFloat()
        val offsetYStep = state.canvasSize.height / steps
        val price = minPrice + ((steps - step) * priceStep)
        val text = TextLine.make(price.toLabelPrice(minPrice.rangeTo(maxPrice)), TextRendering.fontAxis)
        val topOffset = step * offsetYStep
        if (topOffset < text.height) return@PriceAxisContentTemplate
        nativeCanvas.drawTextLine(
            text, canvasSize.width - text.width - DashboardSizes.VerticalAxisHorizontalPadding.toPx(), topOffset + text.descent, TextRendering.paint
        )
    })
}

@Composable
private fun Grid(state: PriceBoardState) {
    val items = state.priceItems
    val canvasSize = state.canvasSize
    val steps = state.verticalSteps()
    val offsetYStep = state.canvasSize.height / steps

    PriceAxisContentTemplate(items, state, horizontalContent = { priceItem, _ ->
        val bottomAxisHeight = state.bottomAxisBarHeight()
        drawLine(DashboardColors.GridLine, start = Offset(0f, -canvasSize.height), end = Offset(0f, -bottomAxisHeight))
    }, verticalContent = { step, _ ->
        val topOffset = step * offsetYStep
        drawLine(DashboardColors.GridLine, start = Offset(0f, topOffset), end = Offset(canvasSize.width, topOffset))
    })
}

@Composable
private fun PriceAxisContentTemplate(
    items: List<PriceItem>, state: PriceBoardState, horizontalContent: DrawScope.(PriceItem?, step: Int) -> Unit, verticalContent: DrawScope.(step: Int, steps: Int) -> Unit
) {
    Canvas {
        //Axis X
        clipRectSafe(right = state.verticalPriceBarLeft()) {
            withTranslateAndScaleDefaults(state, translateTop = size.height, scaleY = 1f, flipY = false) {
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
    val items = state.priceItems
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
            Point(x, 0f).normalize(viewPort).transformNormToReal(size).x
        } else state.pointer.x


        //vertical
        if (state.pointer.x <= verticalPriceBarLeft) {
            drawLine(
                DashboardColors.MouseCross, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = DashboardSizes.MouseCrossStrokeWidth.toPx(), pathEffect = effect
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
            "Offset:[${state.offset.x.f3}, ${state.offset.y.f3}]",
            "Mouse:[${state.pointer.x.toInt()}, ${(canvasSize.height - state.pointer.y).toInt()}] " + "N[${nPointer.x.f3}, ${nPointer.y.f3}] =>" + "V[${vPointer.x.f3}, ${vPointer.y.f3}] => " + "N[${nPointer2.x.f3}, ${nPointer2.y.f3}] =>" + "R[${rPointer.x.f3}, ${rPointer.y.f3}]",
            "Canvas:[${canvasSize.width.toInt()},${canvasSize.height.toInt()}]",
            "Scale:[${state.scale.x.f3},${state.scale.y.f3}]",
            "ViewPort:[${viewPort.toLTRBWH()}]",
            "Mouse: Index=${state.selectedPriceItemIndex()}, Price:${state.mousePrice()}",
            "Data: Items:${state.priceItems.size}, LastItemPriceCenter:${state.priceItems.lastOrNull()?.centerY?.f3}",
        )
        drawIntoCanvas {
            translate(left = 4f, top = 100.dp.toPx()) {
                rows.forEachIndexed { index, s ->
                    it.nativeCanvas.drawTextLine(TextLine.make(s, TextRendering.font), 0f, index * TextRendering.font.metrics.height, TextRendering.paint)
                }
            }
        }

        val verticalPriceBarLeft = state.verticalPriceBarLeft()
        drawLine(Color.Magenta, start = Offset(0f, canvasSize.height / 2), end = Offset(canvasSize.width, canvasSize.height / 2))
        drawLine(Color.Magenta, start = Offset(verticalPriceBarLeft / 2, 0f), end = Offset(verticalPriceBarLeft / 2, canvasSize.height))

        withTranslateAndScale(state) {
            val size = 100f
            drawLine(Color.Green, start = Offset(0f, -size / state.scale.y), end = Offset(0f, size / state.scale.y))
            drawLine(Color.Green, start = Offset(-size / state.scale.x, 0f), end = Offset(size / state.scale.x, 0f))
        }
    }
}

@Composable
private fun Canvas(modifier: Modifier = Modifier, content: DrawScope.() -> Unit) {
    Spacer(modifier = modifier.fillMaxSize().drawBehind {
        clipRectSafe {
            content()
        }
    })
}

private fun textTradingAverages(
    avgMarketPrice: BigDecimal,
    avgBuyPrice: BigDecimal,
    avgSellPrices: BigDecimal,
    market: Color,
    tradingColor: StateContainer<Color>
) = AnnotatedString.Builder().apply {
    append("M", separator = null, avgMarketPrice, market)
    append("B", separator = null, avgBuyPrice, tradingColor.default2)
    append("S", separator = null, avgSellPrices, tradingColor.default)
}.toAnnotatedString()

private fun textVolumeStats(
    asset: Asset,
    coin1SumViewPort: BigDecimal?,
    coin2SumViewPort: BigDecimal?,
    coin1SumDay: BigDecimal?,
    coin2SumDay: BigDecimal?,
    tradingColor: StateContainer<Color>
) = AnnotatedString.Builder().apply {
    append(asset.coin1, ":", coin1SumViewPort, tradingColor.default2If(coin1SumViewPort?.isZeroOrPositive ?: false))
    append(asset.coin2, ":", coin2SumViewPort, tradingColor.default2If(coin2SumViewPort?.isZeroOrPositive ?: false))
    if (coin1SumDay.isNotNullAndNotZero() && coin2SumDay.isNotNullAndNotZero() && length > 0) {
        append(",")
    }
    append(asset.coin1, ":", coin1SumDay, tradingColor.default2If(coin1SumDay?.isZeroOrPositive ?: false))
    append(asset.coin2, ":", coin2SumDay, tradingColor.default2If(coin2SumDay?.isZeroOrPositive ?: false))
}.toAnnotatedString()

private fun AnnotatedString.Builder.append(prefix: String, separator: String?, amount: BigDecimal?, amountColor: Color) {
    if (amount.isNotNullAndNotZero()) {
        if (length > 0) append(" ")
        append(prefix)
        if (separator != null) {
            append(separator)
        }
        append(AnnotatedString(amount.hrs(), SpanStyle(color = amountColor)))
    }
}