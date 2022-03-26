package com.scurab.ptracker

import MainWindowViewModel
import com.scurab.ptracker.component.KoinViewModelFactory
import com.scurab.ptracker.component.navigation.ComponentFactory
import com.scurab.ptracker.component.navigation.DefaultNavSpecs
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.ui.settings.SettingsViewModel
import com.scurab.ptracker.usecase.LoadDataUseCase
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import org.koin.dsl.module

fun createKoinModule(appArgs: Array<String>) = module {
    single { AppStateRepository() }
    single<ComponentFactory> { KoinViewModelFactory() }

    single { defaultNavSpecs(appArgs, get()) }
    single<NavSpecs> { get<DefaultNavSpecs>() }
    single<NavController> { get<DefaultNavSpecs>() }

    factory { LoadDataUseCase() }
    factory { LoadLedgerUseCase() }
    factory { MainWindowViewModel(get(), get()) }
    factory { PriceBoardViewModel(get(), get(), get()) }
    factory { args -> SettingsViewModel(args.get(), get()) }
}


