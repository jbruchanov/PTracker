package com.scurab.ptracker.ui.main

import androidx.compose.ui.input.key.Key
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.navigation.StartNavToken
import com.scurab.ptracker.model.Ledger
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.usecase.LoadLedgerUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

interface MainEventHandler {
    fun onOpenPriceDashboardClick()
    fun onOpenSettingsClick()
    fun onOpenStatsClick()
    fun onKeyPressed(key: Key): Boolean
}

class MainViewModel(
    private val appStateRepository: AppStateRepository,
    private val loadLedgerUseCase: LoadLedgerUseCase,
    private val navController: NavController
) : ViewModel(), MainEventHandler {

    fun density() = appStateRepository.density

    init {
        launch(Dispatchers.IO) {
            appStateRepository.setLedger(runCatching { loadLedgerUseCase.load(File("data/output.xlsx")) }.getOrDefault(Ledger.Empty))
        }
    }

    override fun onOpenSettingsClick() {
        navController.push(AppNavTokens.Settings)
    }

    override fun onOpenStatsClick() {
        navController.push(AppNavTokens.Stats)
    }

    override fun onOpenPriceDashboardClick() {
        navController.popTo(StartNavToken)
    }

    override fun onKeyPressed(key: Key): Boolean {
        appStateRepository.onKey(key)
        return true
    }
}