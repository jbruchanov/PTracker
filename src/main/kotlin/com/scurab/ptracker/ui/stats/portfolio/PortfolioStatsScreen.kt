package com.scurab.ptracker.ui.stats.portfolio

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.coloredMarketPercentage
import com.scurab.ptracker.app.ext.hrs
import com.scurab.ptracker.app.ext.pieChartData
import com.scurab.ptracker.app.ext.scaled
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.MarketPercentage
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.component.compose.onMouseMove
import com.scurab.ptracker.component.util.mock
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppShapes
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.FSpacer
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.LineChart
import com.scurab.ptracker.ui.common.PieChart
import com.scurab.ptracker.ui.common.PieChartSegment
import com.scurab.ptracker.ui.common.WSpacer2
import com.scurab.ptracker.ui.common.WSpacer4
import com.scurab.ptracker.ui.stats.portfolio.PortfolioStatsUiState.Companion.MarketPercentageGroupingThreshold
import stub.StubData
import java.math.BigDecimal

class PortfolioStatsUiState() {
    var isLoading by mutableStateOf(false)
    var cryptoHoldings = mutableStateListOf<OnlineHoldingStats>()
    var marketPercentage by mutableStateOf<List<MarketPercentage>>(emptyList())
    var pieChartData by mutableStateOf<List<PieChartSegment>>(emptyList())
    var selectedHoldingsAsset by mutableStateOf<Asset?>(null)
    var primaryCoin by mutableStateOf<String?>(null)
    var coinSumPerExchange by mutableStateOf<Map<String, List<CoinExchangeStats>>>(emptyMap())
    var feesPerCoin = mutableStateMapOf<String, BigDecimal>()
    var portfolioChartUiState by mutableStateOf<PortfolioChartUiState>(PortfolioChartUiState.Loading)

    fun isHoldingsSelected(onlineHoldingStats: OnlineHoldingStats) = onlineHoldingStats.asset == selectedHoldingsAsset

    companion object {
        const val IconToGrayscaleDelay = 120_000L
        const val MarketPercentageGroupingThreshold = 0.1f
    }
}


interface StatsEventHandler {
    fun onHoldingsRowClicked(index: Int, onlineHoldingStats: OnlineHoldingStats)
    fun onFiatRowClicked(fiatCoin: FiatCoin)
}

@Composable
fun Modifier.statsContentBackground(shape: Shape = AppTheme.Shapes.RoundedCornersSize4): Modifier {
    return padding(AppSizes.current.Space)
        .border(AppSizes.current.ThinLine, AppColors.current.PrimaryVariant, shape)
        .background(AppColors.current.RowBackground.get(), shape)
        .clip(shape)
}

@Composable
fun PortfolioStatsScreen(vm: PortfolioStatsViewModel) {
    Column {
        val state = vm.uiState
        Text(text = LocalTexts.current.Stats + (state.primaryCoin?.let { " - $it" } ?: ""),
            style = AppTheme.TextStyles.Header,
            modifier = Modifier.padding(AppSizes.current.Space2))
        Box(modifier = Modifier.padding(AppSizes.current.Space2)) {
            PortfolioStatsScreen(vm.uiState, vm)
        }
    }
}

@Composable
private fun PortfolioStatsScreen(state: PortfolioStatsUiState, event: StatsEventHandler) {
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()
    BoxWithConstraints(modifier = Modifier) {
        Column(modifier = Modifier.verticalScroll(vScrollState).horizontalScroll(hScrollState)) {
            this@BoxWithConstraints.PortfolioChartContent(state, event)
            WSpacer4()
            Row(modifier = Modifier) {
                PortfolioStatsHoldings(state, event)
                WSpacer4()
                FSpacer()
                StatsPieChart(state)
            }
        }
        VerticalScrollbar(rememberScrollbarAdapter(vScrollState), modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd))
        HorizontalScrollbar(rememberScrollbarAdapter(hScrollState), modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter))
    }
}

@Composable
private fun BoxWithConstraintsScope.PortfolioChartContent(state: PortfolioStatsUiState, event: StatsEventHandler) {
    val scrollBarSize = LocalScrollbarStyle.current.thickness
    Box(
        modifier = Modifier
            .requiredHeightIn(min = 320.dp, max = max(320.dp, maxHeight / 3f) - scrollBarSize)
            .requiredWidthIn(max = maxWidth - scrollBarSize)
            .fillMaxWidth()
            .statsContentBackground()
            .padding(top = AppSizes.current.Space6)
    ) {
        when (val chartState = state.portfolioChartUiState) {
            is PortfolioChartUiState.NoPrimaryCurrency -> Text(LocalTexts.current.NoPrimaryCurrencyChart, modifier = Modifier.align(Alignment.Center))
            is PortfolioChartUiState.Error -> Text(chartState.msg, modifier = Modifier.align(Alignment.Center))
            is PortfolioChartUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is PortfolioChartUiState.Data -> PortfolioChart(chartState)
        }
    }
}

@Composable
fun PortfolioChart(chartState: PortfolioChartUiState.Data) {
    val sizes = AppSizes.current
    val data = chartState.chartData
    val density = LocalDensity.current.density
    val pathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(sizes.Space2.toPx(density), sizes.Space2.toPx(density))) }
    val pathEffectLatest = remember { PathEffect.dashPathEffect(floatArrayOf(sizes.Space05.toPx(density), sizes.Space05.toPx(density))) }
    val gradientColor = AppColors.current.RedGreen.default2If(chartState.chartData.hasProfit)
    BoxWithConstraints {
        val radius = remember { 5.dp.toPx(density) }
        var selectedIndex by remember { mutableStateOf(-1) }
        val colorGreen = AppColors.current.CandleGreen
        val colorRed = AppColors.current.CandleRed
        val colorOnBackground = AppColors.current.OnBackground
        Box(modifier = Modifier
            .onMouseMove(data.marketPrice.size) { m, index -> selectedIndex = index }
        ) {
            LineChart(
                data.marketPrice,
                style = Stroke(AppSizes.current.ThickLine.toPx()),
                strokeColor = colorGreen,
                listOf(gradientColor, Color.Transparent)
            )

            LineChart(
                data.latestMarketPrice,
                style = Stroke(AppSizes.current.ThinLine.toPx(), pathEffect = pathEffectLatest),
                strokeColor = colorOnBackground
            )

            LineChart(
                data.cost,
                style = Stroke(AppSizes.current.ThickLine.toPx(), pathEffect = pathEffect),
                strokeColor = colorRed
            )


            val styleStroke: DrawStyle = Stroke(AppSizes.current.ThickLine.toPx(density))
            Canvas(modifier = Modifier.fillMaxSize()) {
                data.marketPrice.getOrNull(selectedIndex)?.let {
                    drawCircle(
                        colorOnBackground,
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
            }

            data.stats.getOrNull(selectedIndex)?.let { stats ->
                Row(
                    modifier = Modifier
                        .offset(x = 0.dp, y = -AppSizes.current.Space)
                        .statsContentBackground(AppShapes.current.RoundedCornersSize2)
                        .padding(AppSizes.current.Space2, AppSizes.current.Space)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(stats.formattedDateTime, style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
                    WSpacer2()
                    Text("+${stats.marketPrice.hrs()}", style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
                    WSpacer2()
                    Text("-${stats.cost.hrs()}", style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
                    WSpacer2()
                    Text(stats.percents, style = AppTheme.TextStyles.SmallMonospace, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatsPieChart(state: PortfolioStatsUiState) {
    Box(modifier = Modifier.size(300.dp).padding(AppSizes.current.Space8)) {
        PieChart(state.pieChartData)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center).width(intrinsicSize = IntrinsicSize.Max)
        ) {
            Text("%", color = AppColors.current.PrimaryVariant, fontSize = AppTheme.TextRendering.xlarge)
        }
    }
    WSpacer4()
    Column(
        modifier = Modifier
            .statsContentBackground()
            .padding(AppSizes.current.Space4)
            .width(200.dp)
    ) {
        var dividerShown = false
        state.marketPercentage.scan(0f) { acc: Float, (asset, perc, color) ->
            if (!dividerShown && acc >= 1f - MarketPercentageGroupingThreshold) {
                Divider(color = AppColors.current.Primary, thickness = AppSizes.current.ThickLine)
                HSpacer()
                dividerShown = true
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    Box(modifier = Modifier.size(15.dp.scaled()).background(color).align(Alignment.CenterVertically))
                    WSpacer2()
                    Text(asset.cryptoLabelOnlyIf(true), maxLines = 1)
                }
                Text((perc.toBigDecimal() * 100.bd).hrs() + "%", textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.width(75.dp))
            }
            HSpacer()
            acc + perc
        }
    }
}


@Preview
@Composable
private fun PreviewStatsScreen() {
    AppTheme {
        val uiState = PortfolioStatsUiState().apply {
            this.cryptoHoldings.addAll(StubData.onlineStubHoldings())
            this.marketPercentage = StubData.onlineStubHoldings().coloredMarketPercentage(0f)
            this.pieChartData = this.marketPercentage.pieChartData(0f)
        }
        BoxWithConstraints {
            PortfolioStatsScreen(uiState, StatsEventHandler::class.mock())
        }
    }
}