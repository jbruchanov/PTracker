package com.scurab.ptracker.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterfallChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.scurab.ptracker.App.getKoin
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.NavigationScope
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.component.navigation.NavToken
import com.scurab.ptracker.model.WsMessageToken
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.common.VerticalDivider
import com.scurab.ptracker.ui.common.VerticalTabButton
import kotlinx.coroutines.delay

private data class LeftMenuButton(
    val imageVector: ImageVector, val token: NavToken<*>, val onClick: () -> Unit
)

class MainUiState {
    var latestPriceTick by mutableStateOf<WsMessageToken?>(null)
}

@Composable
fun Main(vm: MainViewModel) {
    Main(vm.uiState, vm)
}

@Composable
private fun Main(uiState: MainUiState, eventHandler: MainEventHandler) {
    val navigation = remember { getKoin().get<NavSpecs>(qualifier = NavigationScope.Main) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(eventHandler) {
        focusRequester.requestFocus()
    }
    Box(modifier = Modifier.focusable().focusRequester(focusRequester).onKeyEvent {
        eventHandler.onKeyPressed(it.key)
    }) {
        Row {
            val navToken by navigation.activeScreen.collectAsState()
            Column(
                modifier = Modifier.width(IntrinsicSize.Max).zIndex(1000f)
            ) {
                Menu(navToken, uiState.latestPriceTick, eventHandler)
            }
            VerticalDivider()
            Box(
                modifier = Modifier.zIndex(1f).weight(1f)
            ) {
                navigation.render()
            }
        }
    }
}

@Composable
private fun ColumnScope.Menu(navToken: NavToken<*>, tick: WsMessageToken?, eventHandler: MainEventHandler) {
    val buttons = remember {
        listOf(
            LeftMenuButton(Icons.Default.WaterfallChart, AppNavTokens.PriceDashboard, eventHandler::onOpenPriceDashboardClick),
            LeftMenuButton(Icons.Default.DataUsage, AppNavTokens.Stats, eventHandler::onOpenStatsClick),
            LeftMenuButton(Icons.Default.Settings, AppNavTokens.Settings, eventHandler::onOpenSettingsClick),
        )
    }
    buttons.forEach { (icon, token, handler) ->
        VerticalTabButton(icon, isSelected = navToken == token, onClick = handler)
        Divider(color = AppTheme.Colors.PrimaryVariant)
    }
    Spacer(modifier = Modifier.weight(1f))
    PriceTickShape(tick)
}

@Composable
private fun PriceTickShape(tick: WsMessageToken?) {
    var scale by remember { mutableStateOf(1f) }
    var color by remember(tick?.timestamp) { mutableStateOf(AppTheme.DashboardColors.Candle.default) }

    LaunchedEffect(tick?.timestamp, color) {
        val scalePeak = 1.25f
        Animatable(1f).animateTo(scalePeak, animationSpec = tween(300)) { scale = this.value }
        Animatable(scalePeak).animateTo(1f, animationSpec = tween(300)) { scale = this.value }
    }
    LaunchedEffect(tick?.timestamp) {
        delay(30000)
        color = AppTheme.DashboardColors.Candle.default2
    }
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = AppSizes.current.minClickableSize(), minHeight = AppSizes.current.minClickableSize())
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(AppSizes.current.Space2))
                .background(color)
                .size(AppSizes.current.Space4)

        )
    }
}