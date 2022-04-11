package com.scurab.ptracker

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scurab.ptracker.app.ext.toPx
import com.scurab.ptracker.app.model.Point
import com.scurab.ptracker.app.repository.AppStateRepository
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.English
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.LineChart
import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import java.awt.Toolkit
import kotlin.random.Random

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
                LocalDensity provides density, LocalTexts provides English
            ) {
                AppTheme {
                    navigation()
//                    TestChart()
                }
            }
        }
    }

    fun exitApplication() {

    }
}

@Composable
@Preview
private fun TestChart() {
    val vScrollState = rememberScrollState()
    val hScrollState = rememberScrollState()
    BoxWithConstraints(modifier = Modifier.background(Color.White)) {
        Column(modifier = Modifier.background(Color.Red).fillMaxHeight().verticalScroll(vScrollState).horizontalScroll(hScrollState)) {
            Box(
                modifier = Modifier
                    .background(color = Color.DarkGray)
                    //.requiredWidthIn(max = this@BoxWithConstraints.maxWidth)
                    .fillMaxWidth()
                    .requiredHeightIn(min = 480.dp, max = this@BoxWithConstraints.maxHeight / 3f)
//                    .weight(0.33f, fill = false)
            ) {
                val steps = 40
                val random = Random(10)
                val sampleData = (0..steps).map {
                    Point(it.toFloat() / steps, 0.5f + (random.nextInt(-4, 4) / 10f))
                }
                LineChart(
                    sampleData,
                    style = Stroke(5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 10.dp.toPx()))),
                    strokeColor = Color.White,
                    fillingGradientColors = listOf(Color.White, Color.Black)
                )
            }
            Column(
                modifier = Modifier
                    .background(color = Color.Blue)
                    .requiredHeightIn(min = 480.dp)
            ) {
                repeat(20) {
                    Text("$it".repeat(100), maxLines = 1)
                    HSpacer()
                }
            }
        }
        VerticalScrollbar(rememberScrollbarAdapter(vScrollState), modifier = Modifier.fillMaxHeight())
        HorizontalScrollbar(rememberScrollbarAdapter(hScrollState), modifier = Modifier.fillMaxWidth())
    }
}
