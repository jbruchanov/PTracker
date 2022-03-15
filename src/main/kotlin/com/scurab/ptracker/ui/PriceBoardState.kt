package com.scurab.ptracker.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.ext.maxValue
import com.scurab.ptracker.ext.nHeight
import com.scurab.ptracker.ext.nWidth
import com.scurab.ptracker.ext.normalize
import com.scurab.ptracker.ext.scale
import com.scurab.ptracker.ext.toPx
import com.scurab.ptracker.ext.transformNormToViewPort
import com.scurab.ptracker.model.IPriceItem
import com.scurab.ptracker.model.PriceItem
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.skia.FontMetrics
import org.jetbrains.skia.Point
import java.awt.Cursor
import java.lang.Float.min
import kotlin.math.ceil
import kotlin.math.max

class PriceBoardState(items: List<PriceItem>, private val localDensity: Density) {
    var scale by mutableStateOf(Offset(1f, 1f))
    var offset by mutableStateOf(Offset.Zero)
    var pointer by mutableStateOf(Point.ZERO)
    var canvasSize by mutableStateOf(Size.Zero)
    var pointedPriceItem by mutableStateOf<PriceItem?>(null)
    var items by mutableStateOf(items)

    var mouseIcon by mutableStateOf(PointerIcon(Cursor(Cursor.CROSSHAIR_CURSOR)))
    var isChangingScale by mutableStateOf(false)

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

    fun initViewport(size: Size): Rect {
        val lastItem = items.lastOrNull() ?: return Rect(0f, 0f, size.width, size.height)
        val allColumnsWidth = (items.size * PriceDashboardSizes.PriceItemWidth)
        //take last n visible items on the screen
        val sample = items.takeLast((size.width / PriceDashboardSizes.PriceItemWidth).toInt())
        val y = lastItem.centerPrice
        val avgHeight = sample.map { it.rectSize.height }.average().toFloat()
        val scaleX = (size.width / PriceDashboardConfig.DefaultMinColumns / PriceDashboardSizes.PriceItemWidth)
            .coerceIn(PriceDashboardConfig.ScaleRangeX[0], PriceDashboardConfig.ScaleRangeX[1])
        val scaleY = (avgHeight * 0.25f / lastItem.rectSize.height)
            .coerceIn(PriceDashboardConfig.MinInitScaleY, PriceDashboardConfig.MaxInitScaleY)
        return Rect(0f, size.height, size.width, 0f)
            .scale(scaleX, scaleY)
            .translate(allColumnsWidth - verticalPriceBarLeft(), y - size.height / 2)
    }

    suspend fun setViewport(viewport: Rect, size: Size = this.canvasSize, animate: Boolean = false) {
        require(!size.isEmpty()) { "Size has 0 values" }
        val offset = Offset(viewport.left, viewport.bottom)
        val scale = Offset(viewport.nWidth / size.width, viewport.nHeight / size.height)
        if (animate) {
            animateToOffsetScale(offset, scale)
        } else {
            this.offset = offset
            this.scale = scale
        }
    }

    fun selectedPriceItemIndex() = ceil(viewportPointer().x / PriceDashboardSizes.PriceItemWidth).toInt() - 1
    fun verticalPriceBarLeft(): Float = canvasSize.width - PriceDashboardSizes.VerticalPriceBarWidth.toPx(this.localDensity.density)
    fun bottomAxisBarHeight(metrics: FontMetrics = TextRendering.fontLabels.metrics): Float =
        max(metrics.height + metrics.bottom, PriceDashboardSizes.BottomAxisContentMinHeight.toPx(localDensity.density))

    suspend fun animateToOffsetScale(offset: Offset = this.offset, scale: Offset = this.scale) = coroutineScope {
        launch { Animatable(this@PriceBoardState.offset, Offset.VectorConverter).animateTo(offset, animationSpec = tween(300)) { this@PriceBoardState.offset = value } }
        launch { Animatable(this@PriceBoardState.scale, Offset.VectorConverter).animateTo(scale, animationSpec = tween(300)) { this@PriceBoardState.scale = value } }
    }

    fun chartScalePivot() = Offset(offset.x + verticalPriceBarLeft(), -offset.y - canvasSize.height / 2)
    fun viewport() = Rect(left = 0f, top = canvasSize.height, right = canvasSize.width, bottom = 0f)
        //offset & height (to move origin to bottom/left)
        .translate(offset.x, offset.y)
        //scale in same way as we do for preview
        .scale(1f / scale.x, 1f / scale.y, pivot = chartScalePivot().let { it.copy(-it.x) })

    companion object {
        private val ONE = Offset(1f, 1f)
    }
}
