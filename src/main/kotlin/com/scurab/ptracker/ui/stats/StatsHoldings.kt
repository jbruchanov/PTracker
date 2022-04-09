package com.scurab.ptracker.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.fiatCoins
import com.scurab.ptracker.app.ext.firstIf
import com.scurab.ptracker.app.ext.gf2
import com.scurab.ptracker.app.ext.gf4
import com.scurab.ptracker.app.ext.gf4p
import com.scurab.ptracker.app.ext.imageOrNull
import com.scurab.ptracker.app.ext.isNotLastIndex
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.isPositive
import com.scurab.ptracker.app.ext.percf2
import com.scurab.ptracker.app.ext.scaled
import com.scurab.ptracker.app.ext.totalCost
import com.scurab.ptracker.app.ext.totalGains
import com.scurab.ptracker.app.ext.totalMarketValue
import com.scurab.ptracker.app.ext.totalRoi
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.OnlineHoldingStats
import com.scurab.ptracker.component.compose.StateContainer
import com.scurab.ptracker.component.util.mock
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.DateTimeFormats
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.ColorMatrixGreyScale
import com.scurab.ptracker.ui.common.Divider
import com.scurab.ptracker.ui.common.ExpandableContent
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.HSpacer2
import com.scurab.ptracker.ui.common.HSpacer4
import com.scurab.ptracker.ui.common.WSpacer
import com.scurab.ptracker.ui.common.WSpacer4
import kotlinx.coroutines.delay
import stub.StubData
import java.math.BigDecimal

private object ColumnWidths {
    val Icon = AppTheme.Sizes.StatsIconSize
    val IconCoinGap = 16.dp
    val Coin = StateContainer(40.dp, default2 = 64.dp)
    val Balance = 96.dp
    val Cost = 110.dp
    val MarketValue = 110.dp
    val ROI = 72.dp

    val DetailColumnElementWidthMin = 100.dp
    val DetailContentLabel = 200.dp
    val DetailContentBalance = 110.dp
    val ExchangePerc = 150.dp
}

@Composable
private fun Modifier.defaultTableRow() = padding(horizontal = AppSizes.current.Space4, vertical = AppSizes.current.Space2)

@Composable
private fun RowHeader(hasMultipleFiatCoins: Boolean) {
    val texts = LocalTexts.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.defaultTableRow().defaultMinSize(minHeight = 40.dp)
    ) {
        val col1Width = ColumnWidths.Icon + ColumnWidths.IconCoinGap + ColumnWidths.Coin.default2If(hasMultipleFiatCoins).scaled()
        TextCell(texts.Asset, isMonoSpace = false, textAlign = TextAlign.Center, width = col1Width)
        TextCell(texts.Balance, isMonoSpace = false, width = ColumnWidths.Balance.scaled())
        TextCell(texts.Cost, isMonoSpace = false, width = ColumnWidths.Cost.scaled())
        TextCell(texts.Price, isMonoSpace = false, width = ColumnWidths.Cost.scaled())
        TextCell(texts.MarketValue, isMonoSpace = false, width = ColumnWidths.MarketValue.scaled())
        WSpacer4()
        TextCell(texts.ROI, isMonoSpace = false, textAlign = TextAlign.Center, width = ColumnWidths.ROI.scaled())
        WSpacer4()
    }
}

@Composable
private fun RowFooter(
    onClick: () -> Unit,
    fiatCoin: FiatCoin,
    hasMultipleFiats: Boolean,
    state: StatsUiState,
    exchangeCoverage: List<CoinExchangeStats>,
    selected: Boolean
) {
    val holdings = state.cryptoHoldings
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .defaultMinSize(minHeight = 40.dp)
                .clickable(onClick = onClick)
                .background(AppColors.current.FooterBackground.get(isSelected = selected))
                .defaultTableRow()
        ) {
            CoinIcon(fiatCoin.icon().imageOrNull(), true, 1f)
            WSpacer(ColumnWidths.IconCoinGap)
            TextCell(fiatCoin.item, textAlign = TextAlign.Left, width = ColumnWidths.Coin.default2If(hasMultipleFiats).scaled())
            WSpacer(ColumnWidths.Balance.scaled())
            TextCell(holdings.totalCost(fiatCoin).gf2, width = ColumnWidths.Cost.scaled())
            WSpacer(ColumnWidths.Cost.scaled())
            TextCell(holdings.totalMarketValue(fiatCoin).gf2, width = ColumnWidths.MarketValue.scaled())
            WSpacer4()
            val totalRoi = holdings.totalRoi(fiatCoin)
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(width = ColumnWidths.ROI.scaled())) {
                val color = AppTheme.Colors.RedGreenWhite.get(isEnabled = totalRoi.isNotZero(), isEven = !totalRoi.isPositive)
                TextCell(
                    (totalRoi.gf2 + "%"), color = color, textAlign = TextAlign.Right, width = ColumnWidths.ROI.scaled()
                )
                HSpacer()
                TextCell(
                    holdings.totalGains(fiatCoin).takeIf { it.isNotZero() }?.gf2 ?: "",
                    color = color,
                    textAlign = TextAlign.Right,
                    width = ColumnWidths.ROI.scaled(),
                    style = AppTheme.TextStyles.TinyMonospace
                )
            }
            WSpacer4()
        }
        ExpandableContent(visible = selected) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(AppSizes.current.ThickLine, AppColors.current.SecondaryVariant)
                    .padding(AppSizes.current.Space6)
            ) {
                DetailFiat(fiatCoin, state, exchangeCoverage)
            }
        }
    }
}

@Composable
private fun RowItem(onClick: () -> Unit, index: Int, holdings: OnlineHoldingStats, hasMultipleFiats: Boolean, isSelected: Boolean, modifier: Modifier = Modifier) {
    var scale by remember { mutableStateOf(1f) }
    var isColored by remember(holdings.asset, holdings.timeDate) { mutableStateOf(true) }
    LaunchedEffect(index, holdings.asset, holdings.timeDate) {
        val scalePeak = 1.1f
        Animatable(1f).animateTo(scalePeak, animationSpec = tween(300)) { scale = this.value }
        Animatable(scalePeak).animateTo(1f, animationSpec = tween(500)) { scale = this.value }
    }

    LaunchedEffect(index, holdings.asset, isColored) {
        delay(StatsUiState.IconToGrayscaleDelay)
        isColored = false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(AppColors.current.RowBackground.get(isEven = index % 2 == 0, isSelected = isSelected))
            .padding(horizontal = AppSizes.current.Space4, vertical = AppSizes.current.Space).then(modifier)
    ) {
        CoinIcon(holdings.asset.iconCoin1().imageOrNull(), isColored, scale)
        WSpacer(ColumnWidths.IconCoinGap)
        TextCell(holdings.asset.cryptoLabelOnlyIf(!hasMultipleFiats), textAlign = TextAlign.Left, width = ColumnWidths.Coin.default2If(hasMultipleFiats).scaled())
        TextCell(holdings.actualCryptoBalance.gf4, width = ColumnWidths.Balance.scaled())
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(ColumnWidths.Cost.scaled())) {
            TextCell(holdings.cost.gf2)
            HSpacer()
            TextCell(holdings.costUnit.gf2, color = AppColors.current.SecondaryVariant, style = AppTheme.TextStyles.TinyMonospace)
        }
        TextCell(holdings.marketValueUnitPrice.takeIf { it.isNotZero() }?.gf2 ?: "", width = ColumnWidths.Cost.scaled(), color = AppColors.current.Secondary)
        TextCell(holdings.marketValue.takeIf { it.isNotZero() }?.gf2 ?: "", width = ColumnWidths.MarketValue.scaled())
        WSpacer4()
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(width = ColumnWidths.ROI.scaled())) {
            val color = AppTheme.Colors.RedGreenWhite.get(isEnabled = holdings.roi.isNotZero(), isEven = !holdings.roi.isPositive)
            TextCell(holdings.roi.takeIf { holdings.marketValueUnitPrice.isNotZero() }?.gf2?.let { "$it%" } ?: "", color = color, textAlign = TextAlign.Right)
            HSpacer()
            TextCell(
                holdings.gain.takeIf { it.isNotZero() }?.gf2 ?: "",
                color = color,
                textAlign = TextAlign.Right,
                width = ColumnWidths.ROI.scaled(),
                style = AppTheme.TextStyles.TinyMonospace
            )
        }
        WSpacer4()
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
private fun CoinIcon(image: ImageBitmap?, isColored: Boolean, scale: Float) {
    Box(
        modifier = Modifier.size(ColumnWidths.Icon).border(AppSizes.current.ThinLine, AppColors.current.PrimaryVariant, AppTheme.Shapes.RoundedCornersSize2)
            .clip(AppTheme.Shapes.RoundedCornersSize2).background(AppColors.current.BackgroundAssetIcon).padding(AppSizes.current.Space)
    ) {
        val grayscaleColorFilter = remember { ColorFilter.colorMatrix(ColorMatrixGreyScale) }
        if (image != null) {
            Image(
                image,
                contentDescription = "",
                filterQuality = FilterQuality.High,
                colorFilter = grayscaleColorFilter.takeIf { !isColored },
                modifier = Modifier.scale(scale).align(Alignment.Center)
            )
        } else {
            Image(
                imageVector = Icons.Default.Api,
                contentDescription = "",
                colorFilter = grayscaleColorFilter.takeIf { !isColored },
                modifier = Modifier.scale(scale).align(Alignment.Center)
            )
            WSpacer(ColumnWidths.Icon)
        }
    }
}

@Composable
private fun TextCell(
    text: CharSequence,
    isMonoSpace: Boolean = true,
    textAlign: TextAlign = TextAlign.End,
    width: Dp = Dp.Unspecified,
    color: Color = Color.Unspecified,
    style: TextStyle? = null,
    modifier: Modifier = Modifier
) {
    val textStyles = remember { Pair(AppTheme.TextStyles.NormalMonospace, AppTheme.TextStyles.Normal) }
    val value = if (text is AnnotatedString) text else AnnotatedString(text.toString())
    Text(
        value, maxLines = 1, style = style ?: textStyles.firstIf(isMonoSpace), textAlign = textAlign, color = color, modifier = Modifier.width(width = width).then(modifier)
    )
}


@Composable
fun Holdings(state: StatsUiState, event: StatsEventHandler, modifier: Modifier = Modifier) {
    val fiatCoins = remember(state.cryptoHoldings.size) { state.cryptoHoldings.fiatCoins() }
    val hasFiatCoins = fiatCoins.size > 1
    Row(
        modifier = Modifier
            .padding(AppSizes.current.Space)
            .border(AppSizes.current.ThinLine, AppColors.current.PrimaryVariant, AppTheme.Shapes.RoundedCornersSize4)
            .background(AppColors.current.RowBackground.get(), AppTheme.Shapes.RoundedCornersSize4)
            .clip(AppTheme.Shapes.RoundedCornersSize4).then(modifier)
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Min).wrapContentSize()
        ) {
            RowHeader(hasFiatCoins)
            Divider(thickness = AppSizes.current.ThickLine, color = AppColors.current.Primary, modifier = Modifier.fillMaxWidth())
            state.cryptoHoldings.forEachIndexed { index, onlineHoldingStats ->
                Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                    val isItemSelected = state.isHoldingsSelected(onlineHoldingStats)
                    DetailHoldingsCryptoItem(
                        onClick = { event.onHoldingsRowClicked(index, onlineHoldingStats) },
                        index = index,
                        onlineHoldingStats = onlineHoldingStats,
                        exchangeCoverage = state.coinSumPerExchange[onlineHoldingStats.asset.cryptoCoinOrNull()?.item ?: ""] ?: emptyList(),
                        selected = isItemSelected,
                        useWideCoinsColumn = hasFiatCoins
                    )
                }
                if (state.cryptoHoldings.isNotLastIndex(index)) {
                    Divider()
                }
            }
            Divider(thickness = AppSizes.current.ThickLine, color = AppColors.current.Primary, modifier = Modifier.fillMaxWidth())
            fiatCoins.forEach {
                RowFooter(
                    onClick = { event.onFiatRowClicked(it) },
                    fiatCoin = it,
                    hasMultipleFiats = hasFiatCoins,
                    state = state,
                    exchangeCoverage = state.coinSumPerExchange[it.item] ?: emptyList(),
                    selected = state.selectedHoldingsAsset?.has("", it.item) ?: false
                )
            }
            HSpacer2()
        }
    }
}

@Composable
private fun ColumnScope.DetailHoldingsCryptoItem(
    onClick: () -> Unit,
    index: Int,
    onlineHoldingStats: OnlineHoldingStats,
    exchangeCoverage: List<CoinExchangeStats>,
    selected: Boolean,
    useWideCoinsColumn: Boolean
) {
    RowItem(onClick = onClick, index, onlineHoldingStats, useWideCoinsColumn, isSelected = selected)
    ExpandableContent(visible = selected) {
        Column(
            modifier = Modifier
                .border(AppSizes.current.ThickLine, AppColors.current.SecondaryVariant)
                .padding(AppSizes.current.Space6)
        ) {
            DetailExchangeCoinStats(exchangeCoverage)
            DetailHoldingsCryptoContent(onlineHoldingStats)
        }
    }
}

@Composable
private fun DetailHoldingsCryptoContent(onlineHoldingStats: OnlineHoldingStats) {
    val texts = LocalTexts.current
    HSpacer4()
    Row(modifier = Modifier.fillMaxWidth()) {
        val showFullCryptoBalance = onlineHoldingStats.actualCryptoBalance != onlineHoldingStats.totalCryptoBalance
        val hasFreeIncome = onlineHoldingStats.freeIncome.isNotZero()
        val hasNoProfitOutcome = onlineHoldingStats.nonProfitableOutcome.isNotZero()
        val hasFees = onlineHoldingStats.feesCrypto.isNotZero()
        if (showFullCryptoBalance || hasFreeIncome || hasNoProfitOutcome) {
            Column(modifier = Modifier) {
                val asset = onlineHoldingStats.asset
                TextCell("", isMonoSpace = false)
                HSpacer()
                TextCell(asset.cryptoCoinOrNull()?.item ?: "", isMonoSpace = false, textAlign = TextAlign.Start)
                TextCell(asset.fiatCoinOrNull()?.item ?: "", isMonoSpace = false, textAlign = TextAlign.Start)
            }
            WSpacer4()
        }
        if (showFullCryptoBalance) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).defaultMinSize(minWidth = ColumnWidths.DetailColumnElementWidthMin)) {
                TextCell(texts.TotalBoughtOwned, isMonoSpace = false, textAlign = TextAlign.Start)
                HSpacer()
                TextCell(onlineHoldingStats.totalCryptoBalance.gf4, color = AppColors.current.Secondary)
                TextCell(onlineHoldingStats.totalMarketValue.gf2)
            }
            WSpacer4()
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).defaultMinSize(minWidth = ColumnWidths.DetailColumnElementWidthMin)) {
                TextCell(texts.TotalBoughtOwned, isMonoSpace = false, textAlign = TextAlign.Start)
                HSpacer()
                TextCell("1.0", color = AppColors.current.Secondary)
                TextCell(onlineHoldingStats.costTotalUnit.gf4)
            }
            WSpacer4()
        }
        if (hasFreeIncome) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).defaultMinSize(minWidth = ColumnWidths.DetailColumnElementWidthMin)) {
                TextCell(texts.FreeIncome, isMonoSpace = false, textAlign = TextAlign.Start)
                HSpacer()
                TextCell(onlineHoldingStats.freeIncome.gf2, color = AppColors.current.Green)
                TextCell(onlineHoldingStats.freeIncomeMarketPrice.gf2, color = AppColors.current.Green)
            }
            WSpacer4()
        }
        if (hasNoProfitOutcome) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).defaultMinSize(minWidth = ColumnWidths.DetailColumnElementWidthMin)) {
                TextCell(texts.NoProfitableOutcome, isMonoSpace = false, textAlign = TextAlign.Start)
                HSpacer()
                TextCell(onlineHoldingStats.nonProfitableOutcome.gf4, color = AppColors.current.Red)
                TextCell(onlineHoldingStats.nonProfitableOutcomeMarketPrice.gf2, color = AppColors.current.Red)
            }
        }
        if (hasFees) {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).defaultMinSize(minWidth = ColumnWidths.DetailColumnElementWidthMin)) {
                TextCell(texts.Fees, isMonoSpace = false, textAlign = TextAlign.Start)
                HSpacer()
                TextCell(onlineHoldingStats.feesCrypto.gf4p(), color = AppColors.current.Red)
                TextCell(onlineHoldingStats.feesCryptoMarketValue.gf2, color = AppColors.current.Red)
            }
        }
    }
}

@Composable
private fun ColumnScope.DetailFiat(fiatCoin: FiatCoin, state: StatsUiState, exchangeCoverage: List<CoinExchangeStats>) {
    val texts = LocalTexts.current
    val hasContent = DetailExchangeCoinStats(exchangeCoverage)
    if (hasContent) {
        HSpacer4()
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        val fees = (state.feesPerCoin[fiatCoin.item] ?: BigDecimal.ZERO)
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.defaultMinSize(minWidth = ColumnWidths.DetailColumnElementWidthMin)) {
            TextCell(texts.Fees, isMonoSpace = false, textAlign = TextAlign.Start)
            HSpacer()
            TextCell(fees.gf2, color = if (fees.isNotZero()) AppColors.current.Red else AppColors.current.OnBackground)
        }
    }
}

@Composable
private fun DetailExchangeCoinStats(exchangeCoverage: List<CoinExchangeStats>): Boolean {
    val texts = LocalTexts.current
    val hasContent = exchangeCoverage.isNotEmpty()
    if (hasContent) {
        Row(modifier = Modifier.padding(AppSizes.current.Space2)) {
            TextCell(texts.ExchangeWallet, isMonoSpace = false, textAlign = TextAlign.Start, modifier = Modifier.width(ColumnWidths.DetailContentLabel))
            WSpacer()
            TextCell(texts.Balance, isMonoSpace = false, modifier = Modifier.width(ColumnWidths.DetailContentBalance))
        }
        Divider(color = AppColors.current.PrimaryVariant, thickness = AppSizes.current.ThickLine)
        exchangeCoverage.forEachIndexed { index, stats ->
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.current.RowBackground.get(isEven = index % 2 == 0))
                    .padding(AppSizes.current.Space2)
            ) {
                TextCell(stats.exchange.item, width = ColumnWidths.DetailContentLabel, textAlign = TextAlign.Start)
                WSpacer()
                TextCell(
                    text = when {
                        !stats.coin.isFiat() -> stats.quantity.gf4
                        stats.coin.isFiat() && stats.quantity >= 0.01.bd -> stats.quantity.gf2
                        else -> stats.quantity.stripTrailingZeros().toPlainString()
                    },
                    width = ColumnWidths.DetailContentBalance,
                    color = if (stats.coin.isFiat()) AppColors.current.OnBackground else AppColors.current.Secondary
                )
                WSpacer()
                TextCell(stats.perc.percf2, width = ColumnWidths.ExchangePerc)
            }
        }
        Divider(color = AppColors.current.PrimaryVariant, thickness = AppSizes.current.ThinLine)
    }
    return hasContent
}


@Preview
@Composable
private fun PreviewHoldings() {
    AppTheme {
        val uiState = StatsUiState().apply {
            this.cryptoHoldings.addAll(StubData.onlineStubHoldings())
            this.pieChartData = StubData.pieChartData()
            this.selectedHoldingsAsset = StubData.AssetBTCGBP
        }
        CompositionLocalProvider(
            LocalDensity provides Density(1.25f, 1f)
        ) {
            Box {
                Holdings(uiState, StatsEventHandler::class.mock())
            }
        }
    }
}

