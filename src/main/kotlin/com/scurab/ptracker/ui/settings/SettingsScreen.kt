package com.scurab.ptracker.ui.settings

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Help
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scurab.ptracker.component.util.mock
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.common.FSpacer
import com.scurab.ptracker.ui.common.HSpacer
import com.scurab.ptracker.ui.common.HSpacer05
import com.scurab.ptracker.ui.common.HSpacer2
import com.scurab.ptracker.ui.common.WSpacer
import com.scurab.ptracker.ui.common.WSpacer2
import com.scurab.ptracker.ui.common.WSpacer4
import com.scurab.ptracker.ui.model.IconColor
import com.scurab.ptracker.ui.model.Validity
import kotlin.math.roundToInt

class SettingsUiState {
    var fontScale by mutableStateOf(1f)
    var cryptoCompareKey by mutableStateOf("")
    var isTestingCryptoCompareKey by mutableStateOf(false)
    var isCryptoCompareKeyValid by mutableStateOf(Validity.Valid)
    var predefinedLedgers = mutableStateListOf<String>()
    var primaryCoin by mutableStateOf<String>("")
    var primaryCoinValidity by mutableStateOf(Validity.Unknown)
    var debug by mutableStateOf(false)

    @Composable
    fun cryptoCompareIcon() = when (isCryptoCompareKeyValid) {
        Validity.Valid -> IconColor(imageVector = Icons.Default.Check, color = Color.White)
        Validity.Tested -> IconColor(imageVector = Icons.Default.Check, color = Color.Green)
        Validity.Invalid -> IconColor(imageVector = Icons.Default.Clear, color = Color.Red)
        Validity.NotTested -> IconColor(imageVector = Icons.Outlined.Help, color = Color.LightGray)
        Validity.Unknown -> null
    }
}

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    Box(
        modifier = Modifier.padding(AppSizes.current.Space2)
    ) {
        SettingsScreen(vm.uiState, vm)
    }
}

@Composable
private fun SettingsScreen(state: SettingsUiState, handler: SettingsEventHandler) {
    val texts = LocalTexts.current
    BoxWithConstraints {
        val vScrollState = rememberScrollState()
        Row(modifier = Modifier) {
            Column(
                modifier = Modifier.defaultMinSize(minHeight = this@BoxWithConstraints.maxHeight)
            ) {
                Row(modifier = Modifier.wrapContentWidth()) {
                    Text(text = texts.Settings, style = AppTheme.TextStyles.Header, modifier = Modifier)
                    FSpacer()
                    Button(onClick = handler::onSaveClicked, modifier = Modifier.align(Alignment.Top)) {
                        Text(LocalTexts.current.Save, maxLines = 1, color = AppColors.current.Secondary)
                    }
                }

                HSpacer2()
                Row {
                    Column(modifier = Modifier.verticalScroll(vScrollState).weight(1f)) {
                        SettingsContent(state, handler)
                    }
                    WSpacer()
                    VerticalScrollbar(adapter = rememberScrollbarAdapter(vScrollState))
                }
            }
        }
    }
}

@Composable
fun ColumnScope.SettingsContent(state: SettingsUiState, handler: SettingsEventHandler) {
    val texts = LocalTexts.current
    val appSizes = AppSizes.current

    Label(text = "${texts.FontScaling}: ${(state.fontScale * 100).roundToInt()}%")
    Row(modifier = Modifier) {
        Slider(
            state.fontScale,
            onValueChange = { handler.onFontScaleChanged(it, false) },
            onValueChangeFinished = { handler.onFontScaleChanged(state.fontScale, true) },
            valueRange = 0.75f.rangeTo(3f),
            modifier = Modifier.requiredWidthIn(120.dp, 320.dp).align(Alignment.CenterVertically)
        )
        WSpacer2()
        Text(LocalTexts.current.TextSample, fontSize = 14.sp * state.fontScale, modifier = Modifier.align(Alignment.CenterVertically))
    }

    Label(text = texts.PrimaryCoin)
    Row {
        TextField(
            value = state.primaryCoin,
            onValueChange = { handler.onPrimaryCoinChanged(it) },
            textStyle = AppTheme.TextStyles.Monospace,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("USD") },
            trailingIcon = {
                val icon = when (state.primaryCoinValidity) {
                    Validity.Valid -> IconColor(imageVector = Icons.Default.Check, color = AppColors.current.Green)
                    Validity.Invalid -> IconColor(imageVector = Icons.Default.Clear, color = AppColors.current.Red)
                    else -> null
                }
                if (icon != null) {
                    Image(imageVector = icon.imageVector.get(), contentDescription = "", colorFilter = ColorFilter.tint(icon.color.get()))
                }
            }
        )
    }

    Label(text = texts.CryptoCompareApiKey)
    Row {
        TextField(
            value = state.cryptoCompareKey,
            onValueChange = { handler.onCryptoCompareKeyChanged(it) },
            textStyle = AppTheme.TextStyles.Monospace,
            modifier = Modifier.fillMaxWidth()
        )
        FSpacer()
        if (false) {
            Button(
                onClick = handler::onTestCryptoCompareKeyClicked,
                enabled = !state.isTestingCryptoCompareKey,
                modifier = Modifier.height(IntrinsicSize.Max)
            ) {
                val icon = state.cryptoCompareIcon()
                val smallSpace = state.isTestingCryptoCompareKey || icon != null
                if (state.isTestingCryptoCompareKey) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else if (icon != null) {
                    Image(icon.imageVector.get(), contentDescription = "", colorFilter = ColorFilter.tint(icon.color.get()))
                }
                WSpacer(if (smallSpace) appSizes.Space2 else appSizes.Space8)
                Text(texts.Test, maxLines = 1, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
    }
    Label(text = texts.Ledgers)
    state.predefinedLedgers.forEachIndexed { index, s ->
        val isLast = index == state.predefinedLedgers.size - 1
        LedgerRow(
            index, s,
            showDeleteButton = true,
            onChange = handler::onLedgerChanged,
            onDelete = handler::onDeleteLedger
        )
        if (!isLast) {
            HSpacer()
        }
    }
}

@Composable
private fun ColumnScope.LedgerRow(
    index: Int,
    path: String,
    showDeleteButton: Boolean,
    onChange: (Int, String) -> Unit,
    onDelete: (Int, String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = path,
            onValueChange = { onChange(index, it) },
            textStyle = AppTheme.TextStyles.Monospace,
            placeholder = { Text("/path/yourledger.xlsx") },
            modifier = Modifier.weight(1f)
        )
        if (showDeleteButton) {
            WSpacer4()
            Button(
                onClick = { onDelete(index, path) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "")
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    HSpacer2()
    Text(text = text)
    HSpacer05()
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    AppTheme {
        val state = SettingsUiState().apply {
            fontScale = 1.5f
            predefinedLedgers.addAll(listOf("Ledger1", "Ledger2", ""))
            cryptoCompareKey = "Abc"
        }
        SettingsScreen(state, SettingsEventHandler::class.mock())
    }
}