package com.scurab.ptracker.ui.settings

import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavArgs
import com.scurab.ptracker.component.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SettingsArgs(val delay: Long) : NavArgs

class SettingsViewModel(
    private val args: SettingsArgs,
    private val navController: NavController
) : ViewModel() {
    init {
        launch {
            delay(args.delay)
            navController.pop()
        }
    }
}