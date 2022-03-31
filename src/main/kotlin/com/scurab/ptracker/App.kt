package com.scurab.ptracker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scurab.ptracker.ui.English
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.main.Main
import com.scurab.ptracker.ui.main.MainViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import java.awt.Toolkit

fun main(args: Array<String>) {
    startKoin { loadKoinModules(createKoinModule(args)) }
    App.start()
}

object App : KoinComponent {
    fun start() = application {
        val resolution = Toolkit.getDefaultToolkit().screenSize
        val viewModel = get<MainViewModel>()
        Window(
            onCloseRequest = ::exitApplication,
            title = LocalTexts.current.AppTitle,
            state = rememberWindowState(size = DpSize((resolution.width * 0.5).dp, (resolution.height * 0.75).dp))
        ) {
            val density by viewModel.density().collectAsState()
            CompositionLocalProvider(
                LocalDensity provides density,
                LocalTexts provides English
            ) {
                Main(viewModel)
            }
        }
    }

    fun exitApplication() {

    }
}
