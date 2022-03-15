import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.scurab.ptracker.model.CryptoComparePriceItem
import com.scurab.ptracker.model.PriceItem
import com.scurab.ptracker.model.randomPriceData
import com.scurab.ptracker.serialisation.JsonBridge
import com.scurab.ptracker.ui.PriceBoard
import com.scurab.ptracker.ui.PriceBoardState
import com.scurab.ptracker.ui.TextRendering
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.awt.Toolkit
import java.io.File
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@Composable
@Preview
fun App() {
    val f = File("data/BTC-GBP.json")
    val items = if (f.exists()) {
        JsonBridge.deserialize<List<CryptoComparePriceItem>>(f.readText()).mapIndexed { index, cryptoComparePriceItem -> PriceItem(index, cryptoComparePriceItem) }
    } else {
        randomPriceData(Random, 100, Clock.System.now().minus(1000L.days).toLocalDateTime(TimeZone.UTC), 1L.days)
    }
    TextRendering.init()
    MaterialTheme {
        val contentPadding = 2.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
        ) {
            Box(modifier = Modifier) {
                val localDensity = LocalDensity.current
                val state = remember { PriceBoardState(items, localDensity) }
                PriceBoard(state)
            }
        }
    }
}

fun main() = application {
    val resolution = Toolkit.getDefaultToolkit().screenSize
    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(
            size = DpSize((resolution.width * 0.5).dp, (resolution.height * 0.75).dp)
        )
    ) {
        App()
    }
}


@Composable
fun TestWindow() {
    Canvas(modifier = Modifier.size(500.dp, 500.dp)) {
        scale(2f, 1f, pivot = Offset(size.width, size.height / 2)) {
            drawRect(Color.Black, size = size)
            DebugGrid()
        }
    }
}

fun DrawScope.DebugGrid() {
    val canvasSize = size
    translate(size.width / 2, size.height / 2) {
        val size = 100f
        drawLine(Color.Magenta, start = Offset(0f, -size), end = Offset(0f, size))
        drawLine(Color.Magenta, start = Offset(-size, 0f), end = Offset(size, 0f))

        drawRect(Color.Magenta, topLeft = Offset(-size / 2, -size / 2), Size(size, size), style = Stroke(width = 1.dp.toPx()))
        rotate(45f, pivot = Offset.Zero) {
            drawRect(Color.Magenta, topLeft = Offset(-size / 2, -size / 2), Size(size, size), style = Stroke(width = 1.dp.toPx()))
        }
    }
}
