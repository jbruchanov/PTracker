package com.scurab.ptracker.ui.stats

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.coloredMarketPercentage
import com.scurab.ptracker.app.ext.gf2
import com.scurab.ptracker.app.ext.pieChartData
import com.scurab.ptracker.app.ext.scaled
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.MarketPercentage
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.component.util.mock
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.LineChart
import com.scurab.ptracker.ui.common.PieChart
import com.scurab.ptracker.ui.common.PieChartSegment
import com.scurab.ptracker.ui.common.WSpacer2
import com.scurab.ptracker.ui.common.WSpacer4
import com.scurab.ptracker.ui.stats.StatsUiState.Companion.MarketPercentageGroupingThreshold
import stub.StubData
import java.math.BigDecimal

class StatsUiState() {
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
fun Modifier.statsContentBackground(): Modifier {
    return padding(AppSizes.current.Space)
        .border(AppSizes.current.ThinLine, AppColors.current.PrimaryVariant, AppTheme.Shapes.RoundedCornersSize4)
        .background(AppColors.current.RowBackground.get(), AppTheme.Shapes.RoundedCornersSize4)
        .clip(AppTheme.Shapes.RoundedCornersSize4)
}

@Composable
fun StatsScreen(vm: StatsViewModel) {
    Column {
        val state = vm.uiState
        Text(text = LocalTexts.current.Stats + (state.primaryCoin?.let { " - $it" } ?: ""),
            style = AppTheme.TextStyles.Header,
            modifier = Modifier.padding(AppSizes.current.Space2))
        Box(modifier = Modifier.padding(AppSizes.current.Space2)) {
            StatsScreen(vm.uiState, vm)
        }
    }
}

@Composable
private fun StatsScreen(state: StatsUiState, event: StatsEventHandler) {
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()
    BoxWithConstraints(modifier = Modifier) {
        Column(modifier = Modifier.verticalScroll(vScrollState).horizontalScroll(hScrollState)) {
            this@BoxWithConstraints.PortfolioChart(state, event)
            WSpacer4()
            Row(modifier = Modifier) {
                Holdings(state, event)
                WSpacer4()
                StatsPieChart(state)
            }
        }
        VerticalScrollbar(rememberScrollbarAdapter(vScrollState), modifier = Modifier.fillMaxHeight().align(Alignment.CenterEnd))
        HorizontalScrollbar(rememberScrollbarAdapter(hScrollState), modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter))
    }
}

@Composable
private fun BoxWithConstraintsScope.PortfolioChart(state: StatsUiState, event: StatsEventHandler) {
    val scrollBarSize = LocalScrollbarStyle.current.thickness
    Box(
        modifier = Modifier
            .requiredHeightIn(min = 320.dp, max = max(320.dp, maxHeight / 3f) - scrollBarSize)
            .requiredWidthIn(max = maxWidth - scrollBarSize)
            .fillMaxWidth()
            .statsContentBackground()
            .padding(top = AppSizes.current.Space6)
    ) {
        val chartState = state.portfolioChartUiState
        val density = LocalDensity.current.density
        val sizes = AppSizes.current
        when (chartState) {
            is PortfolioChartUiState.NoPrimaryCurrency -> Text("NoPrimaryCurrency", modifier = Modifier.align(Alignment.Center))
            is PortfolioChartUiState.Error -> Text(chartState.msg, modifier = Modifier.align(Alignment.Center))
            is PortfolioChartUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is PortfolioChartUiState.Data -> {
                val data = chartState.chartData
                val pathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(sizes.Space2.toPx(density), sizes.Space2.toPx(density))) }
                LineChart(
                    data.marketPrice,
                    style = Stroke(AppSizes.current.ThickLine.toPx()),
                    strokeColor = AppColors.current.Green,
                    listOf(AppColors.current.SecondaryVariant.copy(alpha = 0.5f), Color.Transparent)
                )
                LineChart(
                    data.cost,
                    style = Stroke(AppSizes.current.ThickLine.toPx(), pathEffect = pathEffect),
                    strokeColor = AppColors.current.Red
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatsPieChart(state: StatsUiState) {
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
                Text((perc.toBigDecimal() * 100.bd).gf2 + "%", textAlign = TextAlign.End, maxLines = 1, modifier = Modifier.width(75.dp))
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
        val uiState = StatsUiState().apply {
            this.cryptoHoldings.addAll(StubData.onlineStubHoldings())
            this.marketPercentage = StubData.onlineStubHoldings().coloredMarketPercentage(0f)
            this.pieChartData = this.marketPercentage.pieChartData(0f)
        }
        BoxWithConstraints {
            StatsScreen(uiState, StatsEventHandler::class.mock())
        }
    }
}