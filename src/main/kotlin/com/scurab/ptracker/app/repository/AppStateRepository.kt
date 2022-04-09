package com.scurab.ptracker.app.repository

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository(private val appSettings: AppSettings) {

    private val _appData = MutableStateFlow(AppData.Empty)
    var appData = _appData.asStateFlow()

    private val _selectedAsset = MutableStateFlow(Asset.Empty)
    val selectedAsset = _selectedAsset.asStateFlow()

    private val _latestKeyPress = MutableSharedFlow<Key>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val keyEvents = _latestKeyPress.asSharedFlow()

    private val _density = MutableStateFlow(Density(1f, appSettings.fontScale))
    val density = _density.asStateFlow()

    fun setSelectedAsset(value: Asset) {
        _selectedAsset.tryEmit(value)
        appSettings.lastSelectedAsset = value.takeIf { !it.isEmpty }
    }

    fun onKey(key: Key) {
        _latestKeyPress.tryEmit(key)
    }

    fun setDensity(value: Float) {
        val d = _density.value
        _density.tryEmit(Density(d.density, value))
    }

    fun setAppData(appData: AppData) {
        _appData.tryEmit(appData)
        appSettings.lastSelectedAsset
            ?.takeIf { appData.ledger.assetsTradings.contains(it) }
            ?.let {
                _selectedAsset.tryEmit(it)
            }
    }
}