package com.scurab.ptracker

import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppSettingsJsonRepository
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.app.usecase.LoadDataUseCase
import com.scurab.ptracker.app.usecase.LoadIconsUseCase
import com.scurab.ptracker.app.usecase.LoadLedgerUseCase
import com.scurab.ptracker.app.usecase.LoadPriceHistoryUseCase
import com.scurab.ptracker.app.usecase.PriceBoardDataProcessingUseCase
import com.scurab.ptracker.app.usecase.StatsCalculatorUseCase
import com.scurab.ptracker.app.usecase.StatsChartCalcUseCase
import com.scurab.ptracker.app.usecase.TestCryptoCompareKeyUseCase
import com.scurab.ptracker.component.KoinViewModelFactory
import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.DefaultNavSpecs
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
import com.scurab.ptracker.ui.DateTimeFormats
import com.scurab.ptracker.ui.app.AppViewModel
import com.scurab.ptracker.ui.main.MainViewModel
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.SettingsViewModel
import com.scurab.ptracker.ui.stats.StatsViewModel
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun createKoinModule(appArgs: Array<String>) = module {
    single { JsonBridge }
    single { AppStateRepository(get()) }
    single<ComponentFactory> { KoinViewModelFactory() }

    //region navigation
    single(qualifier = NavigationScope.App) { appNavSpecs(appArgs, get()) }
    single<NavSpecs>(qualifier = NavigationScope.App) { get<DefaultNavSpecs>(qualifier = NavigationScope.App) }
    single<NavController>(qualifier = NavigationScope.App) {
        get<DefaultNavSpecs>(qualifier = NavigationScope.App)
    }

    single(qualifier = NavigationScope.Main) { defaultNavSpecs(get()) }
    single<NavSpecs>(qualifier = NavigationScope.Main) { get<DefaultNavSpecs>(qualifier = NavigationScope.Main) }
    single<NavController>(qualifier = NavigationScope.Main) { get<DefaultNavSpecs>(qualifier = NavigationScope.Main) }
    //endregion

    single { DateTimeFormats }
    single { defaultHttpClient() }
    single<AppSettings> { AppSettingsJsonRepository.default(get()) }
    single { CryptoCompareClient(get(), get(), get()) }
    single { PricesRepository(get(), get()) }

    factory { LoadPriceHistoryUseCase(get(), get()) }
    factory { LoadLedgerUseCase(get()) }
    factory { LoadDataUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { PriceBoardDataProcessingUseCase() }
    factory { LoadIconsUseCase(get(), get(), get()) }
    factory { TestCryptoCompareKeyUseCase(get()) }
    factory { StatsCalculatorUseCase(get()) }
    factory { StatsChartCalcUseCase(get(), get()) }

    fun Scope.getAppNavController() = get<NavController>(NavigationScope.App)
    fun Scope.getMainNavController() = get<NavController>(NavigationScope.Main)

    single { args -> AppViewModel(args.get(), get(), get(), get(), getAppNavController()) }
    factory { MainViewModel(get(), get(), get(), get(), getMainNavController()) }
    factory { PriceBoardViewModel(get(), get(), get(), get()) }
    factory { SettingsViewModel(get(), get(), get(), get(), getMainNavController()) }
    factory { StatsViewModel(get(), get(), get(), get()) }
}