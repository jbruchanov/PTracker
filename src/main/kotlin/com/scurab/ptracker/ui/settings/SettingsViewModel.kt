package com.scurab.ptracker.ui.settings

import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.app.ext.isNotLastIndex
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.usecase.TestCryptoCompareKeyUseCase
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.ui.model.Validity
import kotlinx.coroutines.launch

interface SettingsEventHandler {
    fun onTestCryptoCompareKeyClicked()
    fun onSaveClicked()
    fun onFontScaleChanged(value: Float)
    fun onCryptoCompareKeyChanged(value: String)
    fun onLedgerChanged(index: Int, path: String)
    fun onDeleteLedger(index: Int, path: String)
}

class SettingsViewModel(
    private val appStateRepository: AppStateRepository,
    private val appSettings: AppSettings,
    private val testCryptoCompareKeyUseCase: TestCryptoCompareKeyUseCase,
    private val navController: NavController
) : ViewModel(), SettingsEventHandler {

    val maxLedgers: Int = 3
    val uiState = SettingsUiState()
    private var latestValidCryptoCompare: String? = null

    init {
        with(uiState) {
            fontScale = appSettings.fontScale
            cryptoCompareKey = appSettings.cryptoCompareApiKey ?: ""

            val ledgers: Collection<String> = appSettings.ledgers ?: emptyList()
            predefinedLedgers.addAll(ledgers)
            if (ledgers.size < maxLedgers) {
                predefinedLedgers.add("")
            }
        }
    }

    fun close() {
        navController.pop()
    }

    override fun onFontScaleChanged(value: Float) {
        uiState.fontScale = value
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

    override fun onLedgerChanged(index: Int, path: String) {
        uiState.predefinedLedgers[index] = path
        if (path.isNotBlank() && uiState.predefinedLedgers.size < maxLedgers && index == uiState.predefinedLedgers.size - 1) {
            uiState.predefinedLedgers.add("")
        }
    }

    override fun onDeleteLedger(index: Int, path: String) {
        val ledgers = uiState.predefinedLedgers

        if (ledgers.isNotLastIndex(index)) {
            ledgers.removeAt(index)
        } else {
            ledgers[index] = ""
        }
        if (ledgers.isEmpty()) {
            ledgers.add("")
        }
    }

    override fun onSaveClicked() {
        appSettings.fontScale = uiState.fontScale
        appSettings.cryptoCompareApiKey = uiState.cryptoCompareKey
        appSettings.ledgers = uiState.predefinedLedgers.filter { it.isNotBlank() }.distinct()
        //currently disabled, strange bug here, after scale change, can't click anymore on left menu button :/
        //appStateRepository.setDensity(uiState.fontScale)
        navController.popTo(AppNavTokens.PriceDashboard)
    }
}