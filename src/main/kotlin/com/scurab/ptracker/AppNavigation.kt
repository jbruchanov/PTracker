package com.scurab.ptracker

import com.scurab.ptracker.component.navigation.AppNavArgs
import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.DefaultStartNavToken
import com.scurab.ptracker.component.navigation.EmptyNavArgs
import com.scurab.ptracker.component.navigation.NavArgs
import com.scurab.ptracker.component.navigation.NavToken
import com.scurab.ptracker.component.navigation.StartNavToken
import com.scurab.ptracker.component.navigation.navigation
import com.scurab.ptracker.ui.app.AppScreen
import com.scurab.ptracker.ui.app.AppViewModel
import com.scurab.ptracker.ui.main.MainScreen
import com.scurab.ptracker.ui.main.MainViewModel
import com.scurab.ptracker.ui.priceboard.PriceBoardScreen
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.SettingsScreen
import com.scurab.ptracker.ui.settings.SettingsViewModel
import com.scurab.ptracker.ui.stats.portfolio.PortfolioStatsScreen
import com.scurab.ptracker.ui.stats.portfolio.PortfolioStatsViewModel
import com.scurab.ptracker.ui.stats.trading.TradingStatsScreen
import com.scurab.ptracker.ui.stats.trading.TradingStatsViewModel
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue

sealed class AppNavTokens<T : NavArgs> : NavToken<T> {
    object PriceDashboard : AppNavTokens<AppNavArgs>(), StartNavToken
    object Stats : AppNavTokens<EmptyNavArgs>()

    object LedgerDateStats : AppNavTokens<EmptyNavArgs>()
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
    screen(DefaultStartNavToken) { vm: AppViewModel -> AppScreen(vm) }
    screen(AppNavTokens.Main) { vm: MainViewModel -> MainScreen(vm) }
}

fun defaultNavSpecs(componentFactory: ComponentFactory) = navigation(componentFactory) {
    screen(AppNavTokens.PriceDashboard) { vm: PriceBoardViewModel -> PriceBoardScreen(vm) }
    screen(AppNavTokens.Stats) { vm: PortfolioStatsViewModel -> PortfolioStatsScreen(vm) }
    screen(AppNavTokens.LedgerDateStats) { vm: TradingStatsViewModel -> TradingStatsScreen(vm) }
    screen(AppNavTokens.Settings) { vm: SettingsViewModel -> SettingsScreen(vm) }
}