package com.scurab.ptracker.ui.stats.trading

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollbarAdapter
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.LazyTable
import com.scurab.ptracker.ui.common.ToggleButton
import com.scurab.ptracker.ui.common.ToggleButtons
import com.scurab.ptracker.ui.common.VerticalDivider
import com.scurab.ptracker.ui.model.TableState

@Composable
fun TradingStatsScreen(vm: TradingStatsViewModel) {
    Column {
        Text(
            text = LocalTexts.current.TradingCoinStats,
            style = AppTheme.TextStyles.Header,
            modifier = Modifier.padding(AppSizes.current.Space2)
        )
        TradingStatsStats(vm.uiState, vm)
    }
}

@Composable
private fun TradingStatsStats(uiState: TradingStatsStatsUiState, eventHandler: TradingStatsEventHandler) {
    Column {
        Column(
            modifier = Modifier
                .padding(horizontal = AppSizes.current.Padding)
                .padding(top = AppSizes.current.Padding)
        ) {
            ToggleButtons {
                uiState.assets.forEach { asset ->
                    ToggleButton(text = asset.label,
                        isSelected = asset == uiState.selectedAsset,
                        onClick = { eventHandler.onSelectedAsset(asset) }
                    )
                    VerticalDivider()
                }
            }
            Divider()
            ToggleButtons {
                uiState.coins.forEach { coin ->
                    ToggleButton(text = coin,
                        isSelected = uiState.selectedAsset?.has(coin, "") ?: false,
                        onClick = { eventHandler.onSelectedCoin(coin) }
                    )
                    VerticalDivider()
                }
            }
            Divider()
            ToggleButtons {
                val values = remember { DateGrouping.values() }
                values.forEach { grouping ->
                    ToggleButton(text = grouping.name/*TODO:translate*/,
                        isSelected = grouping == uiState.selectedGroupingKey,
                        onClick = { eventHandler.onSelectedGrouping(grouping) }
                    )
                    VerticalDivider()
                }
            }
        }

        val lazyListState = rememberLazyListState()
        val horizontalScrollState = rememberScrollState()
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            val textAlignment = remember { listOf(Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd, Alignment.Center) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppSizes.current.Padding)
            ) {
                LazyTable(
                    uiState.tableData,
                    tableState = TableState.default(lazyListState, horizontalScrollState),
                    modifier = Modifier.fillMaxWidth(),
                    headerCell = { column ->
                        Text(
                            text = uiState.tableData.header(column),
                            maxLines = 1,
                            style = AppTheme.TextStyles.Small,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { }
                                .padding(vertical = AppSizes.current.Padding)
                        )
                    }
                ) { row, column ->
                    Text(
                        text = uiState.tableData[row, column],
                        style = AppTheme.TextStyles.SmallMonospace,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(textAlignment[column])
                    )
                }
            }
            VerticalScrollbar(
                adapter = ScrollbarAdapter(lazyListState),
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
            )
            HorizontalScrollbar(
                adapter = ScrollbarAdapter(horizontalScrollState),
                modifier = Modifier
                    .width(maxWidth)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}


