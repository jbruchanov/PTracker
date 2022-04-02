package com.scurab.ptracker.ui.main

import androidx.compose.ui.input.key.Key
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.repository.AppStateRepository

interface MainEventHandler {
    fun onOpenPriceDashboardClick()
    fun onOpenSettingsClick()
    fun onOpenStatsClick()
    fun onKeyPressed(key: Key): Boolean
}

class MainViewModel(
    private val appStateRepository: AppStateRepository,
    private val navController: NavController
) : ViewModel(), MainEventHandler {

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