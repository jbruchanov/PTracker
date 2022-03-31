package com.scurab.ptracker.ui.settings

import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.repository.AppSettings
import com.scurab.ptracker.ui.model.Validity
import com.scurab.ptracker.usecase.TestCryptoCompareKeyUseCase
import kotlinx.coroutines.launch

interface SettingsEventHandler {
    fun onTestCryptoCompareKeyClicked()
    fun onSaveClicked()
    fun onCryptoCompareKeyChanged(value: String)
}

class SettingsViewModel(
    private val appSettings: AppSettings,
    private val navController: NavController,
    private val testCryptoCompareKeyUseCase: TestCryptoCompareKeyUseCase
) : ViewModel(), SettingsEventHandler {

    val uiState = SettingsUiState().apply(appSettings)
    private var latestValidCryptoCompare: String? = null

    fun close() {
        navController.pop()
    }

    override fun onTestCryptoCompareKeyClicked() {
        val key = uiState.cryptoCompareKey.takeIf { it.isNotEmpty() } ?: return
        launch {
            uiState.isTestingCryptoCompareKey = true
            uiState.isCryptoCompareKeyValid = testCryptoCompareKeyUseCase.testKey(key)
            uiState.isTestingCryptoCompareKey = false
        }
    }

    override fun onCryptoCompareKeyChanged(value: String) {
        uiState.cryptoCompareKey = value
        uiState.isCryptoCompareKeyValid = when (value) {
            latestValidCryptoCompare -> Validity.Tested
            appSettings.cryptoCompareApiKey -> Validity.Valid
            "" -> Validity.Unknown
            else -> Validity.NotTested

        }
    }

    override fun onSaveClicked() {
        launch {

        }
    }
}