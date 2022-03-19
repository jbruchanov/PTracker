package com.scurab.ptracker

import com.scurab.ptracker.component.navigation.NavArgs
import com.scurab.ptracker.component.navigation.NavigationToken
import com.scurab.ptracker.component.navigation.StartNavigationToken
import com.scurab.ptracker.component.navigation.ViewModelFactory
import com.scurab.ptracker.component.navigation.navigation
import com.scurab.ptracker.ui.priceboard.PriceBoard
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.Settings
import com.scurab.ptracker.ui.settings.SettingsArgs
import com.scurab.ptracker.ui.settings.SettingsViewModel

sealed class AppNavTokens<T : NavArgs> : NavigationToken<T> {
    object Settings : AppNavTokens<SettingsArgs>()
}

fun defaultNavSpecs(appArgs: Array<String>, viewModelFactory: ViewModelFactory) = navigation(viewModelFactory) {
    appArgs(appArgs)
    screen(StartNavigationToken) { vm: PriceBoardViewModel -> PriceBoard(vm) }
    screen(AppNavTokens.Settings) { vm: SettingsViewModel -> Settings(vm) }
}