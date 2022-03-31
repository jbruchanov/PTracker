package com.scurab.ptracker.ui.priceboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.ext.FloatRange
import com.scurab.ptracker.ext.maxValue
import com.scurab.ptracker.ext.nHeight
import com.scurab.ptracker.ext.nWidth
import com.scurab.ptracker.ext.normalize
import com.scurab.ptracker.ext.scale
import com.scurab.ptracker.ext.takeAround
import com.scurab.ptracker.ext.toPx
import com.scurab.ptracker.ext.transformNormToViewPort
import com.scurab.ptracker.model.Asset
import com.scurab.ptracker.model.GroupStrategy
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.Transaction
import com.scurab.ptracker.ui.AppTheme.DashboardSizes
import com.scurab.ptracker.ui.AppTheme.TextRendering
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.skia.FontMetrics
import org.jetbrains.skia.Point
import java.awt.Cursor
import kotlin.math.ceil
import kotlin.math.max

class PriceBoardState(items: List<PriceItem>, private val localDensity: Density, grouping: GroupStrategy) {
    var scale by mutableStateOf(Offset(1f, 1f))
    var offset by mutableStateOf(Offset.Zero)
    var pointer by mutableStateOf(Point.ZERO)
    var canvasSize by mutableStateOf(Size.Zero)

    var pointedPriceItem by mutableStateOf<PriceItem?>(null)
    var clickedTransaction by mutableStateOf<Pair<Long, Transaction>?>(null)
    var priceItems by mutableStateOf(items)
    var visibleTransactions by mutableStateOf(emptyList<Transaction>())
    var visibleTransactionsPerPriceItem by mutableStateOf(emptyMap<PriceItem, List<Transaction>>())

    //var ledger by mutableStateOf(Ledger.Empty)
    var selectedAsset by mutableStateOf<Asset?>(null)
    val grouping by mutableStateOf(grouping)

    var mouseIcon by mutableStateOf(PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR)))
    var isChangingScale by mutableStateOf(false)
    var isDragging by mutableStateOf(false)
    var animateInitViewPort by mutableStateOf(0L)
    var scrollToTransactionIndex by mutableStateOf(0)
    var highlightTransaction by mutableStateOf<Transaction?>(null)
    var pointingTransaction by mutableStateOf<Transaction?>(null)

    private lateinit var composeCoroutineScope: CoroutineScope

    @Composable
    fun init() {
        composeCoroutineScope = rememberCoroutineScope()
    }

    fun viewportPointer() = pointer.normalize(canvasSize).transformNormToViewPort(viewport())
    fun normalizedPointer() = pointer.normalize(canvasSize)
    fun maxDensity() = localDensity.maxValue()

    suspend fun reset(animate: Boolean = true) = coroutineScope {
        if (animate) {
            animateToOffsetScale(offset = Offset.Zero, scale = ONE)
        } else {
            scale = ONE
            offset = Offset.Zero
        }
    }

    fun initViewport(size: Size = this.canvasSize, priceItemIndex: Int = priceItems.size, alignCenter: Boolean = false): Rect {
        val focusItem = priceItems.getOrNull(priceItemIndex.coerceIn(0, priceItems.size - 1)) ?: return Rect(0f, 0f, size.width, size.height)
        var offsetX = ((priceItemIndex) * DashboardSizes.PriceItemWidth)
        //take last n visible items on the screen
        val sample = priceItems.takeAround(
            priceItemIndex.coerceIn(0, priceItems.size - 1),
            (size.width / DashboardSizes.PriceItemWidth).toInt().coerceAtLeast(10)
        )
        val priceRange = sample.minOf { it.low }.toFloat().rangeTo(sample.maxOf { it.high }.toFloat())
        val y = focusItem.centerY
        val scaleX = (size.width / PriceDashboardConfig.DefaultMinColumns / DashboardSizes.PriceItemWidth)
            .coerceIn(PriceDashboardConfig.ScaleRangeX[0], PriceDashboardConfig.ScaleRangeX[1])

        val maxMinDiff = sample.maxOf { it.high } - sample.minOf { it.low }
        val scaleY = size.height / (1.25f * maxMinDiff.toFloat())
            .coerceIn(PriceDashboardConfig.ScaleRangeY[0], PriceDashboardConfig.ScaleRangeY[1])
        if (alignCenter) {
            offsetX += (size.width - verticalPriceBarWidth() - DashboardSizes.PriceItemWidth) / 2 / scaleX
        }
        return Rect(0f, size.height, size.width, 0f)
            .scale(scaleX, scaleY)
            .translate(offsetX - verticalPriceBarLeft(priceRange), y - size.height / 2)
    }

    fun setViewport(viewport: Rect, size: Size = this.canvasSize, animate: Boolean = false) = composeCoroutineScope.launch {
        require(!size.isEmpty()) { "Size has 0 values" }
        val offset = Offset(viewport.left, viewport.bottom)
        val scale = Offset(viewport.nWidth / size.width, viewport.nHeight / size.height)
        if (animate) {
            animateToOffsetScale(offset, scale)
        } else {
            this@PriceBoardState.offset = offset
            this@PriceBoardState.scale = scale
        }
    }

    fun selectedPriceItem() = priceItems.getOrNull(selectedPriceItemIndex())
    fun selectedPriceItemIndex() = ceil(viewportPointer().x / DashboardSizes.PriceItemWidth).toInt() - 1
    fun verticalPriceBarWidth(priceRange: FloatRange = visiblePriceRange()): Float = max(
        0f, TextRendering.measureAxisWidth(priceRange) + (2 * DashboardSizes.VerticalAxisHorizontalPadding.toPx(localDensity.maxValue()))
    )

    fun verticalPriceBarLeft(priceRange: FloatRange = visiblePriceRange()): Float = max(0f, canvasSize.width - verticalPriceBarWidth(priceRange))

    fun bottomAxisBarHeight(metrics: FontMetrics = TextRendering.fontLabels.metrics): Float =
        max(metrics.height + metrics.bottom, DashboardSizes.BottomAxisContentMinHeight.toPx(localDensity.maxValue()))

    suspend fun animateToOffsetScale(offset: Offset = this.offset, scale: Offset = this.scale) {
        withContext(composeCoroutineScope.coroutineContext) {
            launch {
                Animatable(this@PriceBoardState.offset, Offset.VectorConverter).animateTo(offset, animationSpec = tween(PriceDashboardConfig.ViewportAnimationDuration))
                { this@PriceBoardState.offset = value }
            }
            launch {
                Animatable(this@PriceBoardState.scale, Offset.VectorConverter).animateTo(
                    scale,
                    animationSpec = tween(PriceDashboardConfig.ViewportAnimationDuration)
                ) { this@PriceBoardState.scale = value }
            }
        }
    }

    fun chartScalePivot() = Offset(offset.x + verticalPriceBarLeft(), -offset.y - canvasSize.height / 2)

    fun viewport() = Rect(left = 0f, top = canvasSize.height, right = canvasSize.width, bottom = 0f)
        //offset & height (to move origin to bottom/left)
        .translate(offset.x, offset.y)
        //scale in same way as we do for preview
        .scale(1f / scale.x, 1f / scale.y, pivot = chartScalePivot().let { it.copy(-it.x) })

    fun visiblePriceRange() = Rect(left = 0f, top = canvasSize.height, right = canvasSize.width, bottom = 0f)
        .translate(0f, offset.y)
        .scale(1f, 1f / scale.y, pivot = Offset(0f, -offset.y - canvasSize.height / 2))
        .let { it.bottom.rangeTo(it.top) }

    fun setItems(asset: Asset, items: List<PriceItem>, initViewport: Boolean) {
        selectedAsset = asset
        priceItems = items
        if (initViewport) {
            animateInitViewPort = System.currentTimeMillis()
        }
    }

    companion object {
        private val ONE = Offset(1f, 1f)
    }
}
