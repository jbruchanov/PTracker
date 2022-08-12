package com.scurab.ptracker.ui.stats.chart

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.hrs
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.GroupStatsSum
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppShapes
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.DefaultLabel
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.HSpacer05
import com.scurab.ptracker.ui.common.PriceHistoryChart
import com.scurab.ptracker.ui.common.ToggleButton
import com.scurab.ptracker.ui.common.ToggleButtons
import com.scurab.ptracker.ui.common.VerticalDivider
import com.scurab.ptracker.ui.common.WSpacer
import com.scurab.ptracker.ui.common.WSpacer2
import com.scurab.ptracker.ui.stats.LineChartUiState
import com.scurab.ptracker.ui.stats.portfolio.statsContentBackground

private object ChartStatsScreenDefaults {
    val detailsColumnWidths = listOf(120.dp, 120.dp, 100.dp, 80.dp, 100.dp)
}

@Composable
fun ChartStatsScreen(viewModel: ChartStatsViewModel) {
    Column {
        Text(
            text = LocalTexts.current.TradingCoinStats,
            style = AppTheme.TextStyles.Header,
            modifier = Modifier.padding(AppSizes.current.Space2)
        )
        ChartStatsScreen(viewModel.uiState, viewModel)
    }
}

@Composable
private fun ChartStatsScreen(
    uiState: ChartStatsUiState, eventHandler: ChartStatsEventHandler
) {
    Box(modifier = Modifier) {
        Column(
            modifier = Modifier.padding(AppSizes.current.Padding)
        ) {
            ToggleButtons {
                uiState.assets.forEach { asset ->
                    ToggleButton(
                        text = asset.label,
                        isSelected = asset == uiState.selectedAsset,
                        onClick = { eventHandler.onSelectedAsset(asset) })
                    VerticalDivider()
                }
            }
            Divider()
            ToggleButtons {
                val values = remember { DateGrouping.values().filter { it != DateGrouping.NoGrouping } }
                values.forEach { grouping ->
                    ToggleButton(
                        text = grouping.name/*TODO:translate*/,
                        isSelected = grouping == uiState.selectedGroupingKey,
                        onClick = { eventHandler.onSelectedGrouping(grouping) })
                    VerticalDivider()
                }
            }
            Divider()
            Box(modifier = Modifier) {
                LineChartContent(uiState, eventHandler)
            }
        }
    }
}

@Composable
private fun LineChartContent(uiState: ChartStatsUiState, eventHandler: ChartStatsEventHandler) {
    Box(
        modifier = Modifier.fillMaxSize().statsContentBackground()
    ) {
        val chartState = uiState.chartUiState
        val texts = LocalTexts.current
        when (chartState) {
            is LineChartUiState.NoPrimaryCurrency -> Text(
                LocalTexts.current.NoPrimaryCurrencyChart,
                modifier = Modifier.align(Alignment.Center)
            )

            is LineChartUiState.Error -> Text(chartState.msg, modifier = Modifier.align(Alignment.Center))
            is LineChartUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is LineChartUiState.Data -> PriceHistoryChart(chartState.chartData, bottomContainer = { index ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 0.dp, y = -AppSizes.current.Space)
                ) {
                    val stats = chartState.chartData.stats

                    stats.getOrNull(index)?.let {
                        this@Box.DefaultLabel(it)
                    }
                    val rotation by animateFloatAsState(if (uiState.historyDetailsVisible) 180f else 0f)
                    val today = stats.lastOrNull()
                    if (today != null) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Image(Icons.Default.KeyboardArrowUp,
                                "",
                                colorFilter = ColorFilter.tint(color = AppColors.current.Primary),
                                modifier = Modifier
                                    .rotate(rotation)
                                    .padding(AppSizes.current.Space)
                                    .clip(CircleShape)
                                    .clickable { eventHandler.onExpandCollapseHistoryClick() }
                            )
                            Column(
                                modifier = Modifier
                                    .statsContentBackground(AppShapes.current.RoundedCornersSize2)
                                    .padding(AppSizes.current.Space2, AppSizes.current.Space)
                                    .animateContentSize()
                            ) {
                                if (uiState.historyDetailsVisible) {
                                    val history = remember { chartState.chartData.historyStats(texts) }
                                    DetailRowsTable(history)
                                } else {
                                    DetailRow(texts.Today, today, staticWidth = uiState.historyDetailsVisible)
                                }
                            }
                            //KeyboardArrowUp + horizontal padding to keep it centered
                            WSpacer(32.dp)
                        }
                    }
                }
            })
        }
    }
}

@Composable
private fun DetailRow(label: String, groupStatsSum: GroupStatsSum, staticWidth: Boolean = true) {
    Row {
        Text(
            label,
            style = AppTheme.TextStyles.SmallMonospace,
            modifier = Modifier.run { if (staticWidth) width(120.dp) else this },
            textAlign = TextAlign.End,
            maxLines = 1
        )
        WSpacer2()
        val texts = LocalTexts.current
        val description = remember { groupStatsSum.detail(texts) }
        Text(description, style = AppTheme.TextStyles.SmallMonospace)
    }
}

@Composable
private fun DetailRowsTable(items: List<Pair<String, GroupStatsSum>>) {
    val cols = ChartStatsScreenDefaults.detailsColumnWidths
    val texts = LocalTexts.current
    val colors = AppColors.current
    val hasAvgPricePerCoin = remember(items) { items.any { it.second.avgCryptoPrice.isNotZero() } }
    val spacing = AppSizes.current.Space
    Column(
        modifier = Modifier.width(IntrinsicSize.Max)
    ) {
        Row {
            Cell(texts.Date, width = cols[0])
            WSpacer(spacing)
            Cell(texts.MarketValue, width = cols[1])
            WSpacer(spacing)
            Cell(texts.Cost, width = cols[2])
            WSpacer(spacing)
            Cell("%", width = cols[3])
            if (hasAvgPricePerCoin) {
                WSpacer(spacing)
                Cell(texts.AvgPricePerCoin, width = cols[4])
            }
        }
        HSpacer05()
        Divider(color = AppColors.current.Primary, thickness = AppSizes.current.ThickLine)
        HSpacer05()
        items.forEach { (label, stats) ->
            Row {
                Cell(label, width = cols[0])
                WSpacer(spacing)
                Cell(stats.marketValue.hrs(), color = colors.CandleGreen, width = cols[1])
                WSpacer(spacing)
                Cell("-${stats.cost.hrs()}", color = colors.CandleRed, width = cols[2])
                WSpacer(spacing)
                Cell(stats.percents, color = colors.CandleRed, width = cols[3])
                if (hasAvgPricePerCoin) {
                    WSpacer(spacing)
                    Cell(stats.avgCryptoPrice.hrs(), color = colors.Secondary, width = cols[4])
                }
            }
        }
    }
}

@Composable
private fun Cell(text: String, color: Color = AppColors.current.OnBackground, width: Dp) {
    Cell(AnnotatedString(text), color = color, width = width)
}

@Composable
private fun Cell(text: AnnotatedString, color: Color = AppColors.current.OnBackground, width: Dp) {
    Text(text, color = color, textAlign = TextAlign.Right, style = AppTheme.TextStyles.SmallMonospace, maxLines = 1, modifier = Modifier.width(width))
}

