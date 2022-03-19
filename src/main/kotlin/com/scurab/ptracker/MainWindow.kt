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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.App.getKoin
import com.scurab.ptracker.AppNavTokens
import com.scurab.ptracker.component.ViewModel
import com.scurab.ptracker.component.navigation.NavController
import com.scurab.ptracker.component.navigation.NavSpecs
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.settings.SettingsArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainWindowViewModel(
    private val appStateRepository: AppStateRepository,
    private val navController: NavController
) : ViewModel(), MainWindowHandler {

    override fun onOpenSettingsClick() {
        navController.push(AppNavTokens.Settings, SettingsArgs(2000))
    }
}

interface MainWindowHandler {
    fun onOpenSettingsClick()
}

@Composable
@Preview
fun MainWindow(handler: MainWindowHandler) {
    MaterialTheme {
        val contentPadding = 2.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray)
                .padding(start = contentPadding, bottom = contentPadding, end = contentPadding)
        ) {
            Row {
                Column {
                    Button(onClick = { handler.onOpenSettingsClick() }) {
                        Text("Settings")
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    val navigation = remember { getKoin().get<NavSpecs>() }
                    navigation.render()
                }
            }
        }
    }
}
