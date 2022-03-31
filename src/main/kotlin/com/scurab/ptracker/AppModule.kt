package com.scurab.ptracker

import MainWindowViewModel
import com.scurab.ptracker.component.KoinViewModelFactory
import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.DefaultNavSpecs
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.net.CryptoCompareClient
import com.scurab.ptracker.net.defaultHttpClient
import com.scurab.ptracker.repository.AppSettings
import com.scurab.ptracker.repository.AppSettingsJsonRepository
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.serialisation.JsonBridge
import com.scurab.ptracker.ui.DateTimeFormats
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.SettingsViewModel
import com.scurab.ptracker.usecase.LoadDataUseCase
import com.scurab.ptracker.usecase.LoadIconsUseCase
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import com.scurab.ptracker.usecase.PriceBoardDataProcessingUseCase
import com.scurab.ptracker.usecase.TestCryptoCompareKeyUseCase
import org.koin.dsl.module

fun createKoinModule(appArgs: Array<String>) = module {
    single { AppStateRepository(get()) }
    single<ComponentFactory> { KoinViewModelFactory() }

    single { JsonBridge }
    single { defaultNavSpecs(appArgs, get()) }
    single<NavSpecs> { get<DefaultNavSpecs>() }
    single<NavController> { get<DefaultNavSpecs>() }
    single { DateTimeFormats }
    single { defaultHttpClient() }
    single<AppSettings> { AppSettingsJsonRepository.default(get()) }
    single { CryptoCompareClient(get(), get(), get()) }

    factory { LoadDataUseCase() }
    factory { LoadLedgerUseCase() }
    factory { PriceBoardDataProcessingUseCase() }
    factory { LoadIconsUseCase(get(), get()) }
    factory { TestCryptoCompareKeyUseCase(get()) }

    factory { MainWindowViewModel(get(), get()) }
    factory { PriceBoardViewModel(get(), get(), get(), get()) }
    factory { SettingsViewModel(get(), get(), get()) }
}
