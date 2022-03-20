package com.scurab.ptracker.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun Settings(vm: SettingsViewModel) {
    Column {
        Text("Settings")
        Button(onClick = { vm.close() }) {
            Text("X")
        }
    }
}