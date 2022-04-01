package com.scurab.ptracker.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.component.compose.StateContainer
import com.scurab.ptracker.ext.colored
import com.scurab.ptracker.ext.f2
import com.scurab.ptracker.ext.fiatCoins
import com.scurab.ptracker.ext.firstIf
import com.scurab.ptracker.ext.gf2
import com.scurab.ptracker.ext.gf4
import com.scurab.ptracker.ext.image
import com.scurab.ptracker.ext.isPositive
import com.scurab.ptracker.ext.scaled
import com.scurab.ptracker.ext.totalCost
import com.scurab.ptracker.ext.totalMarketValue
import com.scurab.ptracker.ext.totalRoi
import com.scurab.ptracker.model.FiatCoin
import com.scurab.ptracker.model.OnlineHoldingStats
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.Flag

class StatsUiState {
    var isLoading by mutableStateOf(false)
    var holdings = mutableStateListOf<OnlineHoldingStats>()
}

private object ColumnWidths {
    val Icon = AppTheme.Sizes.StatsIconSize
    val Coin = StateContainer(40.dp, default2 = 64.dp)
    val Balance = 96.dp
    val Cost = 96.dp
    val MarketValue = 96.dp
    val ROI = 72.dp
}

interface StatsEventHandler {

}


@Composable
fun Stats(vm: StatsViewModel) {
    Row(modifier = Modifier) {
        Box(
            modifier = Modifier.padding(AppSizes.current.Space).weight(1f)
        ) {
            Stats(vm.uiState, vm)
        }
    }
}

@Composable
private fun Stats(state: StatsUiState, event: StatsEventHandler) {
    Box {
        val vScrollState = rememberScrollState()
        Row(modifier = Modifier.verticalScroll(vScrollState)) {
            Holdings(state, event)
            Spacer(modifier = Modifier.weight(1f))
        }
        VerticalScrollbar(rememberScrollbarAdapter(vScrollState), modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun HoldingRowHeader(hasMultipleFiatCoins: Boolean) {
    val texts = LocalTexts.current
    Row(
        modifier = Modifier
            .padding(horizontal = AppSizes.current.Space4, vertical = AppSizes.current.Space2)
            .defaultMinSize(minHeight = 40.dp)
    ) {
        val col1Width = ColumnWidths.Icon + AppSizes.current.Space + ColumnWidths.Coin.default2If(hasMultipleFiatCoins).scaled()
        HoldingsText(texts.Asset, isMonoSpace = false, textAlign = TextAlign.Center, width = col1Width)
        HoldingsText(texts.Balance, isMonoSpace = false, width = ColumnWidths.Balance.scaled())
        HoldingsText(texts.Cost, isMonoSpace = false, width = ColumnWidths.Cost.scaled())
        HoldingsText(texts.Price, isMonoSpace = false, width = ColumnWidths.Cost.scaled())
        HoldingsText(texts.MarketValue, isMonoSpace = false, width = ColumnWidths.MarketValue.scaled())
        Spacer(modifier = Modifier.width(AppSizes.current.Space4))
        HoldingsText(texts.ROI, isMonoSpace = false, textAlign = TextAlign.Center, width = ColumnWidths.ROI.scaled())
        Spacer(modifier = Modifier.width(AppSizes.current.Space4))
    }
}

@Composable
private fun HoldingRowFooter(fiatCoin: FiatCoin, hasMultipleFiats: Boolean, holdings: SnapshotStateList<OnlineHoldingStats>) {
    Row(
        modifier = Modifier
            .padding(horizontal = AppSizes.current.Space4, vertical = AppSizes.current.Space2)
    ) {
        Box(modifier = Modifier.width(ColumnWidths.Icon).align(alignment = Alignment.CenterVertically)) {
            Flag(fiatCoin.item, 14.dp, modifier = Modifier.align(Alignment.Center).requiredSize(ColumnWidths.Icon))
        }
        Spacer(modifier = Modifier.width(AppSizes.current.Space))
        HoldingsText(fiatCoin.item, textAlign = TextAlign.Left, width = ColumnWidths.Coin.default2If(hasMultipleFiats).scaled())
        Spacer(modifier = Modifier.background(Color.Black).width(ColumnWidths.Balance.scaled()))
        HoldingsText(holdings.totalCost(fiatCoin).gf2, width = ColumnWidths.Cost.scaled())
        Spacer(modifier = Modifier.width(ColumnWidths.Cost.scaled()))
        HoldingsText(holdings.totalMarketValue(fiatCoin).gf2, width = ColumnWidths.MarketValue.scaled())
        Spacer(modifier = Modifier.width(AppSizes.current.Space4))
        val totalRoi = holdings.totalRoi(fiatCoin)
        HoldingsText(
            (totalRoi.f2 + " %").colored(AppTheme.DashboardColors.Candle.get(isEven = totalRoi.isPositive)),
            textAlign = TextAlign.Right,
            width = ColumnWidths.ROI.scaled()
        )
        Spacer(modifier = Modifier.width(AppSizes.current.Space4))
    }
}

@Composable
private fun HoldingsRow(index: Int, holdings: OnlineHoldingStats, hasMultipleFiats: Boolean) {
    var scale by remember { mutableStateOf(1f) }
    LaunchedEffect(index, holdings.timeDate) {
        val scalePeak = 1.25f
        Animatable(1f).animateTo(scalePeak, animationSpec = tween(300)) { scale = this.value }
        Animatable(scalePeak).animateTo(1f, animationSpec = tween(500)) { scale = this.value }
    }
    Row(
        modifier = Modifier
            .background(AppColors.current.RowBackground.get(isEven = index % 2 == 0))
            .padding(horizontal = AppSizes.current.Space4, vertical = AppSizes.current.Space)
    ) {
        val icon = holdings.asset.iconCrypto()
        if (icon.exists()) {
            Image(icon.image(), contentDescription = "", modifier = Modifier.size(ColumnWidths.Icon).scale(scale))
        } else {
            Spacer(modifier = Modifier.width(ColumnWidths.Icon))
        }
        Spacer(modifier = Modifier.width(AppSizes.current.Space))
        HoldingsText(holdings.asset.cryptoLabelOnlyIf(!hasMultipleFiats), textAlign = TextAlign.Left, width = ColumnWidths.Coin.default2If(hasMultipleFiats).scaled())
        HoldingsText(holdings.actualCryptoBalance.gf4, width = ColumnWidths.Balance.scaled())
        HoldingsText(holdings.cost.gf2, width = ColumnWidths.Cost.scaled())
        HoldingsText(holdings.marketValueUnitPrice.gf2, width = ColumnWidths.Cost.scaled(), color = AppColors.current.Secondary)
        HoldingsText(holdings.marketValue.gf2, width = ColumnWidths.MarketValue.scaled())
        Spacer(modifier = Modifier.width(AppSizes.current.Space4))
        HoldingsText(
            (holdings.roi.f2 + " %").colored(AppTheme.DashboardColors.Candle.get(isEven = holdings.roi.isPositive)),
            textAlign = TextAlign.Right,
            width = ColumnWidths.ROI.scaled()
        )
        Spacer(modifier = Modifier.width(AppSizes.current.Space4))
        if (false) {
            Text(
                DateTimeFormats.fullTime(holdings.timeDate),
                maxLines = 1,
                style = AppTheme.TextStyles.Small,
                textAlign = TextAlign.Center,
                color = AppColors.current.PrimaryVariant,
                modifier = Modifier.align(alignment = Alignment.CenterVertically)
            )
        }
    }
}

@Composable
private fun RowScope.HoldingsText(
    text: CharSequence,
    isMonoSpace: Boolean = true,
    textAlign: TextAlign = TextAlign.Right,
    width: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified
) {
    val textStyles = remember { Pair(AppTheme.TextStyles.NormalMonospace, AppTheme.TextStyles.Normal) }
    val value = if (text is AnnotatedString) text else AnnotatedString(text.toString())
    Text(
        value,
        maxLines = 1,
        style = textStyles.firstIf(isMonoSpace),
        textAlign = textAlign,
        color = color,
        modifier = Modifier.width(width = width).align(alignment = Alignment.CenterVertically)
    )
}

@Composable
private fun Holdings(state: StatsUiState, event: StatsEventHandler, modifier: Modifier = Modifier) {
    val fiatCoins = remember(state.holdings.size) { state.holdings.fiatCoins() }
    val hasFiatCoins = fiatCoins.size > 1
    Row(
        modifier = Modifier
            .padding(AppSizes.current.Space)
            .border(1.dp, AppColors.current.PrimaryVariant, RoundedCornerShape(16.dp))
            .background(AppColors.current.RowBackground.get(), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .then(modifier)
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .wrapContentSize()
        ) {
            HoldingRowHeader(hasFiatCoins)
            Divider(thickness = AppSizes.current.ThickLine, color = AppColors.current.Primary, modifier = Modifier.fillMaxWidth())
            state.holdings.forEachIndexed { index, onlineHoldingStats ->
                HoldingsRow(index, onlineHoldingStats, hasFiatCoins)
                if (index < state.holdings.size - 1) {
                    Divider()
                }
            }
            Divider(thickness = AppSizes.current.ThickLine, color = AppColors.current.Primary, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(AppSizes.current.Space))
            fiatCoins.forEach {
                HoldingRowFooter(it, hasFiatCoins, state.holdings)
            }
            Spacer(modifier = Modifier.height(AppSizes.current.Space2))
        }
    }
}