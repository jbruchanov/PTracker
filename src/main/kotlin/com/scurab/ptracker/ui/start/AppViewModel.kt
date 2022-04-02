package com.scurab.ptracker.ui.start

import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.AppNavArgs
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.repository.AppSettings
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.usecase.LoadDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val appNavArgs: AppNavArgs,
    private val appStateRepository: AppStateRepository,
    private val appSettings: AppSettings,
    private val loadDataUseCase: LoadDataUseCase,
    private val navController: NavController
) : ViewModel(), StartEventHandler {

    val _uiState = MutableStateFlow<AppUiState>(AppUiState.Empty)
    val uiState = _uiState.asStateFlow()

    fun density() = appStateRepository.density

    init {
        launch(Dispatchers.IO) {
            val ledgerLocation = appSettings.latestLedger
            if (ledgerLocation != null) {
                _uiState.tryEmit(AppUiState.Loading)
                runCatching {
                    loadDataUseCase.loadAndSetAllData(ledgerLocation)
                    navController.replace(AppNavTokens.Main)
                }
            } else {
                _uiState.tryEmit(AppUiState.Intro)
            }
        }
    }
}