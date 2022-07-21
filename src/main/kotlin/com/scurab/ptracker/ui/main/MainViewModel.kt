package com.scurab.ptracker.ui.main

import androidx.compose.ui.input.key.Key
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.repository.PricesRepository
import com.scurab.ptracker.app.usecase.LoadDataUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.EmptyNavArgs
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.picker.FilePicker
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

interface MainEventHandler {
    fun onOpenPriceDashboardClicked()
    fun onOpenSettingsClicked()
    fun onOpenStatsClicked()
    fun onOpenLedgerDateStatsClicked()
    fun onKeyPressed(key: Key): Boolean
    fun onLedgerClicked(path: String)
    fun onOpenFileClicked()
}

class MainViewModel(
    private val appStateRepository: AppStateRepository,
    private val appSettings: AppSettings,
    private val pricesRepository: PricesRepository,
    private val loadAllDataUseCase: LoadDataUseCase,
    private val filePicker: FilePicker,
    private val navController: NavController
) : ViewModel(), MainEventHandler {

    val uiState = MainUiState()

    init {
        launch {
            pricesRepository.wsTickToken.collect {
                uiState.latestPriceTick = it
            }
        }
        launch {
            appSettings.flowChanges(AppSettings.KeyLedgers)
                .filter { it == AppSettings.KeyLedgers }
                .collect {
                    uiState.ledgers = appSettings.ledgers ?: emptyList()
                }
        }
        uiState.activeLedger = appSettings.latestLedger
    }

    override fun onOpenSettingsClicked() = replaceMainScreen(AppNavTokens.Settings)
    override fun onOpenStatsClicked() = replaceMainScreen(AppNavTokens.Stats)
    override fun onOpenLedgerDateStatsClicked() = replaceMainScreen(AppNavTokens.LedgerDateStats)

    override fun onOpenPriceDashboardClicked() {
        navController.popTo(AppNavTokens.PriceDashboard)
    }

    override fun onKeyPressed(key: Key): Boolean {
        appStateRepository.onKey(key)
        return true
    }

    override fun onLedgerClicked(path: String) {
        if (uiState.activeLedger == path) return
        launch {
            loadAllDataUseCase.loadAndSetAllData(path)
            uiState.activeLedger = path
        }
    }

    override fun onOpenFileClicked() {
        //filePicker.openFilePicker()
    }

    private fun replaceMainScreen(token: AppNavTokens<EmptyNavArgs>) = with(navController) {
        if (activeScreenNavToken == token) return@with
        popToTop()
        push(token)
    }
}