import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.priceboard.PriceBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class MainWindowState {
    class PriceDashboard(val assets: List<String>) : MainWindowState()
}

class MainWindowViewModel(
    private val appStateRepository: AppStateRepository
) : ViewModel(), MainWindowHandler {
    private val crypto = listOf("BTC", "ETH", "ADA", "LTC", "SOL")
    private val fiat = listOf("GBP", "USD")
    private val pairs = crypto.map { c -> fiat.map { f -> "$c-$f" } }.flatten()

    private val _uiState = MutableStateFlow<MainWindowState>(MainWindowState.PriceDashboard(pairs))
    val uiState = _uiState.asStateFlow()

    override fun onPairSelected(item: String) {
        appStateRepository.setSelectedAsset(item)
    }
}

interface MainWindowHandler {
    fun onPairSelected(item: String)
}

@Composable
@Preview
fun MainWindow(state: MainWindowState, handler: MainWindowHandler) {
    MaterialTheme {
        val contentPadding = 2.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
        ) {
            when (state) {
                is MainWindowState.PriceDashboard -> MainWindowDashboard(state, handler)
            }
        }
    }
}

@Composable
@Preview
private fun MainWindowDashboard(state: MainWindowState.PriceDashboard, handler: MainWindowHandler) {
    var showDialog by remember { mutableStateOf(false) }
    Box(modifier = Modifier) {
        Row {
            Column {
                state.assets.forEach { asset ->
                    Button(onClick = { handler.onPairSelected(asset) }) {
                        Text(asset)
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                PriceBoard()
            }
        }
    }
}
