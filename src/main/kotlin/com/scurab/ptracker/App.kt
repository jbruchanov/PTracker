package com.scurab.ptracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.English
import com.scurab.ptracker.ui.LocalTexts
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import java.awt.Toolkit

fun main(args: Array<String>) {
    startKoin {
        loadKoinModules(createKoinModule(args))
    }
    App.start()
}

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
                LocalTexts provides English
            ) {
                AppTheme {
                    val contentPadding = AppSizes.current.Space05
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(AppColors.current.WindowEdge)
                            .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
                            .background(AppColors.current.BackgroundContent)
                    ) {
                        navigation()
                    }
                }
            }
        }
    }

    fun exitApplication() {

    }
}