package com.scurab.ptracker.ui.main

import androidx.compose.ui.input.key.Key
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.repository.PricesRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

interface MainEventHandler {
    fun onOpenPriceDashboardClick()
    fun onOpenSettingsClick()
    fun onOpenStatsClick()
    fun onKeyPressed(key: Key): Boolean
}

class MainViewModel(
    private val appStateRepository: AppStateRepository,
    private val pricesRepository: PricesRepository,
    private val navController: NavController
) : ViewModel(), MainEventHandler {

    val uiState = MainUiState()

    init {
        launch {
            pricesRepository.wsTickToken.collect {
                uiState.latestPriceTick = it
            }
        }
    }

    override fun onOpenSettingsClick() {
        navController.push(AppNavTokens.Settings)
    }

    override fun onOpenStatsClick() {
        navController.push(AppNavTokens.Stats)
    }

    override fun onOpenPriceDashboardClick() {
        navController.popTo(AppNavTokens.PriceDashboard)
    }

    override fun onKeyPressed(key: Key): Boolean {
        appStateRepository.onKey(key)
        return true
    }
}