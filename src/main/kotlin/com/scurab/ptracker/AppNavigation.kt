package com.scurab.ptracker

import com.scurab.ptracker.component.navigation.AppNavArgs
import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.DefaultStartNavToken
import com.scurab.ptracker.component.navigation.EmptyNavArgs
import com.scurab.ptracker.component.navigation.NavArgs
import com.scurab.ptracker.component.navigation.NavToken
import com.scurab.ptracker.component.navigation.StartNavToken
import com.scurab.ptracker.component.navigation.navigation
import com.scurab.ptracker.ui.main.Main
import com.scurab.ptracker.ui.main.MainViewModel
import com.scurab.ptracker.ui.priceboard.PriceBoard
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.Settings
import com.scurab.ptracker.ui.settings.SettingsViewModel
import com.scurab.ptracker.ui.start.App
import com.scurab.ptracker.ui.start.AppViewModel
import com.scurab.ptracker.ui.stats.Stats
import com.scurab.ptracker.ui.stats.StatsViewModel
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

sealed class AppNavTokens<T : NavArgs> : NavToken<T> {
    object PriceDashboard : AppNavTokens<AppNavArgs>(), StartNavToken
    object Stats : AppNavTokens<EmptyNavArgs>()
    object Settings : AppNavTokens<EmptyNavArgs>()
    object Main : AppNavTokens<EmptyNavArgs>()
}


sealed class NavigationScope : Qualifier {
    override val value: QualifierValue = this.javaClass.name

    object App : NavigationScope()
    object Main : NavigationScope()
}

fun appNavSpecs(appArgs: Array<String>, componentFactory: ComponentFactory) = navigation(componentFactory) {
    appArgs(appArgs)
    screen(DefaultStartNavToken) { vm: AppViewModel -> App(vm) }
    screen(AppNavTokens.Main) { vm: MainViewModel -> Main(vm) }
}

fun defaultNavSpecs(componentFactory: ComponentFactory) = navigation(componentFactory) {
    screen(AppNavTokens.PriceDashboard) { vm: PriceBoardViewModel -> PriceBoard(vm) }
    screen(AppNavTokens.Stats) { vm: StatsViewModel -> Stats(vm) }
    screen(AppNavTokens.Settings) { vm: SettingsViewModel -> Settings(vm) }
}