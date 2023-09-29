package com.scurab.ptracker.ui.app

import com.jibru.kostra.defaultQualifiers
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.K
import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.app.usecase.LoadDataUseCase
import com.scurab.ptracker.component.TextsProvider
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.error.ErrorHandler
import com.scurab.ptracker.component.navigation.AppNavArgs
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.picker.FilePicker
import com.scurab.ptracker.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(
    private val appNavArgs: AppNavArgs,
    private val appStateRepository: AppStateRepository,
    private val appSettings: AppSettings,
    private val loadDataUseCase: LoadDataUseCase,
    private val filePicker: FilePicker,
    private val texts: TextsProvider,
    private val errorHandler: ErrorHandler,
    private val navController: NavController
) : ViewModel(), StartEventHandler {

    val _uiState = MutableStateFlow<AppUiState>(AppUiState.Empty)
    val uiState = _uiState.asStateFlow()

    fun density() = appStateRepository.density

    init {
        launch(Dispatchers.Main) {
            val ledgerLocation = appSettings.latestLedger
            if (ledgerLocation != null) {
                _uiState.tryEmit(AppUiState.Loading)
                withContext(Dispatchers.IO) {
                    loadDataUseCase.loadAndSetAllData(ledgerLocation)
                }
                navController.replace(AppNavTokens.Main)
            } else {
                _uiState.tryEmit(AppUiState.Intro())
            }
        }

        launch(Dispatchers.Main) {
            filePicker.flowResult.collect { (key, uri) ->
                when {
                    key == InitLedger && uri != null -> (uiState.value as AppUiState.Intro).file = uri
                }
            }
        }
    }

    override fun onOpenClicked(file: String) {
        launch(Dispatchers.IO) {
            try {
                _uiState.tryEmit(AppUiState.Loading)
                loadDataUseCase.loadAndSetAllData(file)
                navController.replace(AppNavTokens.Main)
                appSettings.ledgers = listOf(file)
            } catch (e: Throwable) {
                errorHandler.showErrorDialog(K.string.ErrUnableToOpenXlsFile.get(defaultQualifiers(), file), exception = e)
                _uiState.tryEmit(AppUiState.Intro())
            }
        }
    }

    override fun onOpenFileClicked() {
        filePicker.openFilePicker(InitLedger, "*.xlsx")
    }

    companion object {
        private const val InitLedger = "initLedger"
    }
}
