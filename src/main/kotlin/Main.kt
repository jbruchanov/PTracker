import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.scurab.ptracker.model.randomPriceData
import com.scurab.ptracker.ui.PriceBoard
import com.scurab.ptracker.ui.TextRendering
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@Composable
@Preview
fun App() {
    val items = randomPriceData(Random, 10000, Clock.System.now().minus(1000L.days).toLocalDateTime(TimeZone.UTC), 1L.days)
    TextRendering.init()
    MaterialTheme {
        PriceBoard(items)
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}




