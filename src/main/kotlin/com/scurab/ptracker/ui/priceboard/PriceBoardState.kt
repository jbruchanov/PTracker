package com.scurab.ptracker.ui.priceboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.app.ext.FloatRange
import com.scurab.ptracker.app.ext.align
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.coinSum
import com.scurab.ptracker.app.ext.filterVisible
import com.scurab.ptracker.app.ext.getAmount
import com.scurab.ptracker.app.ext.maxValue
import com.scurab.ptracker.app.ext.nHeight
import com.scurab.ptracker.app.ext.nWidth
import com.scurab.ptracker.app.ext.normalize
import com.scurab.ptracker.app.ext.now
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.app.ext.scale
import com.scurab.ptracker.app.ext.takeAround
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.ext.transformNormToViewPort
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CacheRef
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.IPriceItem.Companion.asPriceItem
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.model.PriceItem
import com.scurab.ptracker.app.model.Transaction
import com.scurab.ptracker.app.usecase.PriceItemTransactions
import com.scurab.ptracker.ui.AppTheme.DashboardSizes
import com.scurab.ptracker.ui.AppTheme.TextRendering
import com.scurab.ptracker.ui.model.PriceBoardVisibleStats
import com.scurab.ptracker.ui.model.PriceItemVolumes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import org.jetbrains.skia.FontMetrics
import org.jetbrains.skia.Point
import java.awt.Cursor
import kotlin.math.ceil
import kotlin.math.max

class PriceBoardState(
    items: List<PriceItem>, private val localDensity: Density, grouping: DateGrouping = DateGrouping.Day, val isDebugVisible: Boolean = false
) {
    var scale by mutableStateOf(Offset(1f, 1f))
    var offset by mutableStateOf(Offset.Zero)
    var pointer by mutableStateOf(Point.ZERO)
    var canvasSize by mutableStateOf(Size.Zero)

    var pointedPriceItem by mutableStateOf<PriceItem?>(null)
    var clickedTransaction by mutableStateOf<Pair<Long, Transaction>?>(null)
    var priceItems = SnapshotStateList<PriceItem>().also { it.addAll(items) }
    var transactions by mutableStateOf(emptyList<Transaction>())
    var transactionsPerPriceItem by mutableStateOf(emptyMap<LocalDateTime, PriceItemTransactions>())

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

    private val cachingVisiblePriceItems = CacheRef<Rect, List<PriceItem>>()
    private val cachingVisibleStats = CacheRef<Rect, PriceBoardVisibleStats>()

    private lateinit var composeCoroutineScope: CoroutineScope

    fun init(scope: CoroutineScope) {
        composeCoroutineScope = scope
    }

    fun viewportPointer() = pointer.normalize(canvasSize).transformNormToViewPort(viewport())
    fun normalizedPointer() = pointer.normalize(canvasSize)
    fun maxDensity() = localDensity.maxValue()

    fun resetData() {
        selectedAsset = null
        priceItems.clear()
        clickedTransaction = null
        pointedPriceItem = null
        transactions = emptyList()
        transactionsPerPriceItem = emptyMap()

        resetCache()
    }

    private fun resetCache() {
        cachingVisiblePriceItems.clear()
        cachingVisibleStats.clear()
    }

    suspend fun reset(animate: Boolean = true) = coroutineScope {
        if (animate) {
            animateToOffsetScale(offset = Offset.Zero, scale = ONE)
        } else {
            scale = ONE
            offset = Offset.Zero
        }
    }

    fun initViewport(size: Size = this.canvasSize, priceItemIndex: Int = priceItems.size, alignCenter: Boolean = false): Rect {
        if (priceItems.isEmpty()) return Rect.Zero
        val focusItem = priceItems.getOrNull(priceItemIndex.coerceIn(0, priceItems.size - 1)) ?: return Rect(0f, 0f, size.width, size.height)
        var offsetX = ((priceItemIndex + 1/*1 piece offset to workaround tiny difference*/) * DashboardSizes.PriceItemWidth)
        //take last n visible items on the screen
        val sample = priceItems.takeAround(
            priceItemIndex.coerceIn(0, priceItems.size - 1), PriceDashboardConfig.DefaultMinColumns.coerceAtLeast(10)
        )
        val priceRange = sample.minOf { it.low }.toFloat().rangeTo(sample.maxOf { it.high }.toFloat())
        val y = focusItem.centerY
        val scaleX =
            (size.width / PriceDashboardConfig.DefaultMinColumns / DashboardSizes.PriceItemWidth).coerceIn(PriceDashboardConfig.ScaleRangeX[0], PriceDashboardConfig.ScaleRangeX[1])

        val maxMinDiff = sample.maxOf { it.high } - sample.minOf { it.low }
        val scaleY = size.height / (2.25f /*vertical coef to shrink space to see more*/ * maxMinDiff.toFloat()).coerceIn(
            PriceDashboardConfig.ScaleRangeY[0],
            PriceDashboardConfig.ScaleRangeY[1]
        )
        if (alignCenter) {
            offsetX += (size.width - verticalPriceBarWidth() - DashboardSizes.PriceItemWidth) / 2 / scaleX
        }
        return Rect(0f, size.height, size.width, 0f).scale(scaleX, scaleY).translate(offsetX - verticalPriceBarLeft(priceRange), y - size.height / 2)
    }

    fun setViewport(viewport: Rect, size: Size = this.canvasSize, animate: Boolean = false) = composeCoroutineScope.launch {
        if (size.isEmpty()) return@launch
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
                Animatable(this@PriceBoardState.offset, Offset.VectorConverter).animateTo(
                    offset,
                    animationSpec = tween(PriceDashboardConfig.ViewportAnimationDuration)
                ) { this@PriceBoardState.offset = value }
            }
            launch {
                Animatable(this@PriceBoardState.scale, Offset.VectorConverter).animateTo(
                    scale, animationSpec = tween(PriceDashboardConfig.ViewportAnimationDuration)
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

    fun visiblePriceRange() = Rect(left = 0f, top = canvasSize.height, right = canvasSize.width, bottom = 0f).translate(0f, offset.y)
        .scale(1f, 1f / scale.y, pivot = Offset(0f, -offset.y - canvasSize.height / 2)).let { it.bottom.rangeTo(it.top) }

    fun setItems(asset: Asset, items: List<PriceItem>, initViewport: Boolean) {
        selectedAsset = asset
        priceItems.clear()
        priceItems.addAll(items)
        if (initViewport) {
            animateInitViewPort = System.currentTimeMillis()
        }
    }

    fun updateMarketPrice(marketPrice: MarketPrice) {
        if (selectedAsset == marketPrice.asset) {
            cachingVisiblePriceItems.clear()
            val now = now()
            val today = now.date
            val index = priceItems.indexOfLast { it.item.dateTime.date == today }
            if (index == -1) {
                priceItems.add(PriceItem(priceItems.last().index + 1, marketPrice.asset, marketPrice.asPriceItem(now)))
            } else {
                priceItems[index] = priceItems[index].withCurrentMarketPrice(marketPrice)
            }
        }
    }

    @Composable
    fun getVisiblePriceItems(viewPort: Rect = viewport()): List<PriceItem> {
        return cachingVisiblePriceItems.getOrCreate(viewPort) {
            priceItems.filterVisible(viewPort, endOffset = 1)
        }
    }

    @Composable
    fun getVisibleStats(viewPort: Rect = viewport()): PriceBoardVisibleStats {
        return cachingVisibleStats.coGetOrCreate(viewPort) {
            val visiblePriceItems = getVisiblePriceItems(viewPort)
            val avgMarketPrice = visiblePriceItems.map { it.price }.sumOf { it }.safeDiv(visiblePriceItems.size.bd).align

            val trades = visiblePriceItems.asSequence()
                .mapNotNull {
                    transactionsPerPriceItem[it.dateTime]?.transactions?.filterIsInstance<Transaction.Trade>()
                }
                .flatten()
                .toList()

            val asset = requireNotNull(selectedAsset) { "No asset selected" }

            val (coin1SumBuy, coin1SumSell) = trades.coinSum(asset.coin1)
            val (coin2SumBuy, coin2SumSell) = trades.coinSum(asset.coin2)

            val volumes = visiblePriceItems.associateWith { priceItem ->
                transactionsPerPriceItem[priceItem.dateTime]?.transactions
                    ?.filterIsInstance<Transaction.Trade>()
                    ?.let { trades ->
                        PriceItemVolumes(
                            priceItem,
                            coin1Volume = trades.sumOf { it.getAmount(asset.coin1) },
                            coin2Volume = trades.sumOf { it.getAmount(asset.coin2) },
                        )
                    }
            }

            PriceBoardVisibleStats(
                asset = trades.firstOrNull()?.asset ?: Asset.Empty,
                avgMarketPrice = avgMarketPrice,
                coin1SumBuy = coin1SumBuy,
                coin1SumSell = coin1SumSell,
                coin2SumBuy = coin2SumBuy,
                coin2SumSell = coin2SumSell,
                volumes = volumes,
                transactions = transactions.size
            )
        }
    }

    companion object {
        private val ONE = Offset(1f, 1f)
    }
}
