package com.scurab.ptracker

import MainWindow
import MainWindowViewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scurab.ptracker.component.LocalKoin
import com.scurab.ptracker.ui.TextRendering
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
        TextRendering.init()
        val resolution = Toolkit.getDefaultToolkit().screenSize
        val viewModel = get<MainWindowViewModel>()
        Window(
            onCloseRequest = ::exitApplication, state = rememberWindowState(
                size = DpSize((resolution.width * 0.5).dp, (resolution.height * 0.75).dp)
            )
        ) {
            CompositionLocalProvider(
                LocalKoin provides getKoin(),
            ) {
                MainWindow(viewModel)
            }
        }
    }

    fun exitApplication() {

    }
}
