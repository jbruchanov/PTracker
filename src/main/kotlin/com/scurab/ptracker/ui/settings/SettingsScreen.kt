package com.scurab.ptracker.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.scurab.ptracker.ui.AppSizes
import com.scurab.ptracker.ui.AppTheme
import com.scurab.ptracker.ui.LocalTexts
import com.scurab.ptracker.ui.model.IconColor
import com.scurab.ptracker.ui.model.Validity
import kotlin.math.roundToInt

class SettingsUiState {
    var fontScale by mutableStateOf(1f)
    var cryptoCompareKey by mutableStateOf("")
    var isTestingCryptoCompareKey by mutableStateOf(false)
    var isCryptoCompareKeyValid by mutableStateOf(Validity.Valid)
    var predefinedLedgers = mutableStateListOf<String>()

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
fun Settings(vm: SettingsViewModel) {
    Settings(vm.uiState, vm)
}

@Composable
private fun Settings(state: SettingsUiState, handler: SettingsEventHandler) {
    val texts = LocalTexts.current
    val appSizes = AppSizes.current
    Row(modifier = Modifier.requiredWidthIn(min = 480.dp)) {
        val vScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(vScrollState)
                .weight(1f)
                .padding(AppTheme.Sizes.Space2)
        ) {
            Text(text = texts.Settings, style = AppTheme.TextStyles.Header)

            Spacer(modifier = Modifier.height(appSizes.Space2))

            Label(text = "${texts.FontScaling}: ${(state.fontScale * 100).roundToInt()}%")
            Row {
                Slider(
                    state.fontScale, onValueChange = { handler.onFontScaleChanged(it) }, valueRange = 0.75f.rangeTo(3f),
                    modifier = Modifier.requiredWidthIn(120.dp, 320.dp)
                )
                Spacer(modifier = Modifier.width(AppSizes.current.Space2))
                Text(LocalTexts.current.RestartNeeded, fontSize = 14.sp * state.fontScale)
            }
            Label(text = texts.CryptoCompareApiKey)
            Row {
                TextField(
                    value = state.cryptoCompareKey,
                    onValueChange = { handler.onCryptoCompareKeyChanged(it) },
                    textStyle = AppTheme.TextStyles.Monospace,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(appSizes.Space4))
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
                        Spacer(modifier = Modifier.width(if (smallSpace) appSizes.Space2 else appSizes.Space8))
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
                    Spacer(modifier = Modifier.height(AppSizes.current.Space))
                }
            }

            Spacer(modifier = Modifier.height(appSizes.Space))
            Button(onClick = handler::onSaveClicked, modifier = Modifier.align(Alignment.Start)) {
                Text(LocalTexts.current.Save)
            }
        }
        VerticalScrollbar(adapter = rememberScrollbarAdapter(vScrollState))
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
    Row(modifier = Modifier) {
        TextField(
            value = path,
            onValueChange = { onChange(index, it) },
            textStyle = AppTheme.TextStyles.Monospace,
            placeholder = { Text("/path/yourledger.xlsx") },
            modifier = Modifier
        )
        if (showDeleteButton) {
            Spacer(modifier = Modifier.width(AppSizes.current.Space4))
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
    Spacer(modifier = Modifier.height(AppSizes.current.Space2))
    Text(text = text)
    Spacer(modifier = Modifier.height(AppSizes.current.Space05))
}