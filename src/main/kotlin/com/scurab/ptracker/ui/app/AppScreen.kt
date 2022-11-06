package com.scurab.ptracker.ui.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.scurab.ptracker.app.ext.existsAndHasSize
import com.scurab.ptracker.component.compose.rememberDropFileTarget
import com.scurab.ptracker.component.util.mock
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.VerticalTabButton
import com.scurab.ptracker.ui.common.WSpacer
import org.jetbrains.skiko.MainUIDispatcher
import java.io.File

sealed class AppUiState {
    object Empty : AppUiState()
    object Loading : AppUiState()
    class Intro : AppUiState() {
        var file by mutableStateOf("")
        val isOpenEnabled by derivedStateOf { File(file).let { it.isFile && it.existsAndHasSize() } }
    }
}

interface StartEventHandler {
    fun onOpenClicked(file: String)
    fun onOpenFileClicked()
}

@Composable
fun AppScreen(vm: AppViewModel) {
    val state by vm.uiState.collectAsState(context = MainUIDispatcher)
    AppScreen(state, vm)
}

@Composable
fun AppScreen(uiState: AppUiState, eventHandler: StartEventHandler) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            AppUiState.Empty -> {
            }
            AppUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is AppUiState.Intro -> {
                Column(modifier = Modifier.padding(AppSizes.current.Padding)) {
                    Text("Intro", style = AppTheme.TextStyles.Header)
                    HSpacer(AppSizes.current.Padding)
                    Text(LocalTexts.current.SelectBittyTaxFile)
                    HSpacer(AppSizes.current.Padding05)

                    rememberDropFileTarget {
                        uiState.file = it.firstOrNull()?.absolutePath ?: ""
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = uiState.file,
                            onValueChange = { uiState.file = it },
                            textStyle = AppTheme.TextStyles.Monospace,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(LocalTexts.current.FullPath) },
                        )
                        WSpacer(AppSizes.current.Padding)
                        VerticalTabButton(Icons.Default.FolderOpen, isSelected = false, onClick = { eventHandler.onOpenFileClicked() })
                    }
                    HSpacer(AppSizes.current.Padding05)
                    Button(
                        onClick = { eventHandler.onOpenClicked(uiState.file) },
                        enabled = uiState.isOpenEnabled,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(LocalTexts.current.Open)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun AppScreenPreview() {
    AppTheme {
        AppScreen(AppUiState.Intro(), StartEventHandler::class.mock())
    }
}
