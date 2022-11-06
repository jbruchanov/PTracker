package com.scurab.ptracker.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.hrs
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.maxValue
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.model.GroupStatsSum
import com.scurab.ptracker.app.model.PriceHistoryChartData
import com.scurab.ptracker.app.model.PriceItemUI
import com.scurab.ptracker.component.compose.onMouseMove
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppShapes
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.stats.portfolio.statsContentBackground

@Composable
fun BoxScope.DefaultLabel(stats: GroupStatsSum, dayPriceItemUI: PriceItemUI?) {
    Row(
        modifier = Modifier
            .offset(x = 0.dp, y = -AppSizes.current.Space)
            .statsContentBackground(AppShapes.current.RoundedCornersSize2)
            .padding(AppSizes.current.Space2, AppSizes.current.Space)
            .align(Alignment.BottomCenter)
    ) {
        Text(stats.formattedDateTime, style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
        WSpacer2()
        dayPriceItemUI?.let { priceItemUI ->
            Text(priceItemUI.price.hrs(), style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
            WSpacer2()
        }
        Text(stats.marketValue.hrs(), style = AppTheme.TextStyles.SmallMonospace, maxLines = 1, color = AppColors.current.CandleGreen)
        WSpacer2()
        Text("-${stats.cost.abs().hrs()}", style = AppTheme.TextStyles.SmallMonospace, maxLines = 1, color = AppColors.current.CandleRed)
        WSpacer2()
        Text(stats.percents, style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
        if (stats.avgCryptoPrice.isNotZero()) {
            WSpacer2()
            Text(stats.avgCryptoPrice.hrs(), style = AppTheme.TextStyles.SmallMonospace, maxLines = 1, color = AppColors.current.Secondary)
        }
    }
}

@Composable
fun PriceHistoryChart(
    data: PriceHistoryChartData,
    bottomContainer: @Composable BoxScope.(Int) -> Unit = { data.stats.getOrNull(it)?.let { stats -> DefaultLabel(stats, null) } },
    modifier: Modifier = Modifier
) {
    val sizes = AppSizes.current
    val density = LocalDensity.current.maxValue()
    val spacePx = remember(density) { sizes.Space2.toPx(density) }
    val pathEffect1 = remember { PathEffect.dashPathEffect(floatArrayOf(spacePx, spacePx)) }
    val pathEffect2 = remember { PathEffect.dashPathEffect(floatArrayOf(spacePx, spacePx), spacePx) }
    val pathEffectLatest = remember { PathEffect.dashPathEffect(floatArrayOf(sizes.Space.toPx(density), sizes.Space.toPx(density))) }
    val gradientColor = AppColors.current.RedGreen.default2If(data.hasProfit).copy(alpha = 0.5f)
    val colorGreen = AppColors.current.CandleGreen
    val colorRed = AppColors.current.CandleRed
    val colorOrange = AppColors.current.Secondary
    BoxWithConstraints(modifier = modifier) {
        val radius = remember { 5.dp.toPx(density) }
        var selectedIndex by remember { mutableStateOf(-1) }
        val colorOnBackground = AppColors.current.OnBackground
        Box(
            modifier = Modifier
                .padding(top = AppSizes.current.Space6)
                .onMouseMove(data.marketPrice.size) { m, index -> selectedIndex = index }
        ) {
            LineChart(
                data.marketPrice,
                style = Stroke(AppSizes.current.ThickLine.toPx()),
                strokeColor = colorGreen,
                listOf(gradientColor, Color.Transparent)
            )

            val styleStroke: DrawStyle = Stroke(AppSizes.current.ThickLine.toPx(density))
            Canvas(modifier = Modifier.fillMaxSize()) {
                data.avg.getOrNull(selectedIndex)?.let {
                    drawCircle(
                        colorOrange,
                        radius = radius,
                        style = styleStroke,
                        center = Offset(it.x * size.width, it.y * size.height)
                    )
                }

                data.cost.getOrNull(selectedIndex)?.let {
                    drawCircle(
                        colorRed,
                        radius = radius,
                        style = styleStroke,
                        center = Offset(it.x * size.width, it.y * size.height)
                    )
                }

                data.marketPrice.getOrNull(selectedIndex)?.let {
                    drawCircle(
                        colorOnBackground,
                        radius = radius,
                        style = styleStroke,
                        center = Offset(it.x * size.width, it.y * size.height)
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                data.maxMarketPrice?.let { p ->
                    drawLine(
                        color = colorGreen,
                        start = Offset(p.x * size.width, p.y * size.height),
                        end = Offset(data.maxMarketPrice.x * size.width, size.height),
                        pathEffect = pathEffect2
                    )
                }
                data.minMarketPriceXSinceMax?.let { p ->
                    drawLine(
                        color = colorRed,
                        start = Offset(p.x * size.width, p.y * size.height),
                        end = Offset(p.x * size.width, size.height),
                        pathEffect = pathEffect2
                    )
                }
            }

            LineChart(
                data.latestMarketPrice,
                style = Stroke(AppSizes.current.ThinLine.toPx(), pathEffect = pathEffectLatest),
                strokeColor = colorOnBackground
            )

            LineChart(
                data.cost,
                style = Stroke(AppSizes.current.ThickLine.toPx(), pathEffect = pathEffect1),
                strokeColor = colorRed
            )

            LineChart(
                data.avg,
                style = Stroke(AppSizes.current.ThickLine.toPx(), pathEffect = pathEffect2),
                strokeColor = colorOrange
            )

            bottomContainer(selectedIndex)
        }
    }
}
