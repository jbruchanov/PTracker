package com.scurab.ptracker

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.English
import com.scurab.ptracker.ui.LocalTexts
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import java.awt.Toolkit

fun previewStartKoin() {
    runCatching {
        startKoin {
            loadKoinModules(createKoinModule(emptyArray()))
        }
    }
}

fun main(args: Array<String>) {
    startKoin {
        loadKoinModules(createKoinModule(args))
    }
    App.start()
}

val LocalWindow = staticCompositionLocalOf<ComposeWindow?> { null }

object App : KoinComponent {
    fun start() = application {
        val resolution = Toolkit.getDefaultToolkit().screenSize
        val navigation = remember { getKoin().get<NavSpecs>(qualifier = NavigationScope.App) }
        val appStateRepo = remember { getKoin().get<AppStateRepository>() }
        Window(
            onCloseRequest = ::exitApplication,
            title = LocalTexts.current.AppTitle,
            state = rememberWindowState(size = DpSize((resolution.width * 0.5).dp, (resolution.height * 0.75).dp))
        ) {
            val density by appStateRepo.density.collectAsState()

            CompositionLocalProvider(
                LocalDensity provides density,
                LocalTexts provides English,
                LocalWindow provides window
            ) {
                AppTheme {
                    navigation()
                }
            }
        }
    }

    fun exitApplication() {

    }
}

object Log {
    fun d(tag: String, msg: String) {
        println("$tag $msg")
    }
}
