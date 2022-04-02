package com.scurab.ptracker.ui.start

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

sealed class AppUiState {
    object Empty : AppUiState()
    object Loading : AppUiState()
    object Intro : AppUiState()
}

interface StartEventHandler {

}

@Composable
fun App(vm: AppViewModel) {
    val state by vm.uiState.collectAsState()
    App(state, vm)
}

@Composable
fun App(uiState: AppUiState, eventHandler: StartEventHandler) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            AppUiState.Empty -> {
            }
            AppUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            AppUiState.Intro -> {
                Column {
                    Text("Intro")
                    Text("- pick file")
                    Text("- get CryptoCompare API key")
                }
            }
        }
    }
}