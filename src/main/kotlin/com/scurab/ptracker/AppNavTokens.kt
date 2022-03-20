package com.scurab.ptracker

import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.NavArgs
import com.scurab.ptracker.component.navigation.NavToken
import com.scurab.ptracker.component.navigation.StartNavToken
import com.scurab.ptracker.component.navigation.navigation
import com.scurab.ptracker.ui.priceboard.PriceBoard
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.Settings
import com.scurab.ptracker.ui.settings.SettingsArgs
import com.scurab.ptracker.ui.settings.SettingsViewModel

sealed class AppNavTokens<T : NavArgs> : NavToken<T> {
    object PriceDashboard : AppNavTokens<SettingsArgs>()
    object Settings : AppNavTokens<SettingsArgs>()
}

fun defaultNavSpecs(appArgs: Array<String>, componentFactory: ComponentFactory) = navigation(componentFactory) {
    appArgs(appArgs)
    screen(StartNavToken) { vm: PriceBoardViewModel -> PriceBoard(vm) }
    screen(AppNavTokens.PriceDashboard) { vm: PriceBoardViewModel -> PriceBoard(vm) }
    screen(AppNavTokens.Settings) { vm: SettingsViewModel -> Settings(vm) }
}