import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.ui.priceboard.PriceBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class MainWindowState {
    object PriceDashboard : MainWindowState()
}

class MainWindowViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MainWindowState>(MainWindowState.PriceDashboard)
    val uiState = _uiState.asStateFlow()
}

@Composable
@Preview
fun MainWindow(state: MainWindowState) {
    MaterialTheme {
        val contentPadding = 2.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
        ) {
            Box(modifier = Modifier) {
                when (state) {
                    is MainWindowState.PriceDashboard -> PriceBoard()
                }
            }
        }
    }
}


