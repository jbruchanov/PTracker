import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.scurab.ptracker.model.randomPriceData
import com.scurab.ptracker.ui.PriceBoard
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration.Companion.days

@Composable
@Preview
fun App() {
    val items = randomPriceData(Random, 1000, Clock.System.now().toLocalDateTime(TimeZone.UTC), 1L.days)
    MaterialTheme {
        PriceBoard(items)
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}




