package com.scurab.ptracker.ui.main

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterfallChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.scurab.ptracker.App.getKoin
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.component.navigation.NavToken
import com.scurab.ptracker.component.navigation.StartNavToken
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.common.VerticalDivider
import com.scurab.ptracker.ui.common.VerticalTabButton

private data class LeftMenuButton(
    val imageVector: ImageVector,
    val token: NavToken<*>,
    val onClick: () -> Unit
)

@Composable
@Preview
fun Main(delegate: MainEventHandler) {
    val navigation = remember { getKoin().get<NavSpecs>() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(delegate) {
        focusRequester.requestFocus()
    }
    AppTheme {
        val contentPadding = 2.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.current.WindowEdge)
                .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
                .background(AppColors.current.BackgroundContent)
                .focusable()
                .focusRequester(focusRequester)
                .onKeyEvent {
                    delegate.onKeyPressed(it.key)
                }
        ) {
            Row {
                val buttons = remember {
                    listOf(
                        LeftMenuButton(Icons.Default.WaterfallChart, StartNavToken, delegate::onOpenPriceDashboardClick),
                        LeftMenuButton(Icons.Default.DataUsage, AppNavTokens.Stats, delegate::onOpenStatsClick),
                        LeftMenuButton(Icons.Default.Settings, AppNavTokens.Settings, delegate::onOpenSettingsClick),
                    )
                }
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .zIndex(1000f)
                ) {
                    val navToken by navigation.activeScreen.collectAsState()
                    buttons.forEach { (icon, token, handler) ->
                        VerticalTabButton(icon, isSelected = navToken == token, onClick = handler)
                        Divider(color = AppTheme.Colors.PrimaryVariant)
                    }
                }
                VerticalDivider()
                Box(
                    modifier = Modifier
                        .zIndex(1f)
                        .weight(1f)
                ) {
                    navigation.render()
                }
            }
        }
    }
}
