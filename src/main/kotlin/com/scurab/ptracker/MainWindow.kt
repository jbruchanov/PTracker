import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.twotone.AccountBox
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
import com.scurab.ptracker.component.navigation.StartNavToken
import com.scurab.ptracker.repository.AppStateRepository
import com.scurab.ptracker.ui.common.ImageButton
import com.scurab.ptracker.ui.settings.SettingsArgs

class MainWindowViewModel(
    private val appStateRepository: AppStateRepository,
    private val navController: NavController
) : ViewModel(), MainWindowHandler {

    override fun onOpenSettingsClick() {
        navController.push(AppNavTokens.Settings, SettingsArgs(2000))
    }

    override fun onOpenPriceDashboardClick() {
        navController.popTo(StartNavToken)
    }
}

interface MainWindowHandler {
    fun onOpenPriceDashboardClick()
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
                    ImageButton(Icons.TwoTone.AccountBox, onClick = handler::onOpenPriceDashboardClick)
                    ImageButton(Icons.Default.Settings, onClick = handler::onOpenSettingsClick)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.weight(1f)) {
                    val navigation = remember { getKoin().get<NavSpecs>() }
                    navigation.render()
                }
            }
        }
    }
}
