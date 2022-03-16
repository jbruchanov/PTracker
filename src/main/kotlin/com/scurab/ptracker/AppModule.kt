package com.scurab.ptracker

import MainWindowViewModel
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.priceboard.PriceBoardViewModel
import com.scurab.ptracker.usecase.LoadDataUseCase
import org.koin.dsl.module

val koinModule = module {

    single { AppStateRepository() }

    factory { LoadDataUseCase() }
    factory { MainWindowViewModel() }
    factory { PriceBoardViewModel(get(), get()) }
}