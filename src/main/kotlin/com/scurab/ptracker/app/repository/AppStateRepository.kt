package com.scurab.ptracker.app.repository

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Ledger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository(private val appSettings: AppSettings) {

    private var appData: AppData? = null

    private val _selectedAsset = MutableStateFlow(Asset.Empty)
    val selectedAsset = _selectedAsset.asStateFlow()

    private val _latestKeyPress = MutableSharedFlow<Key>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val keyEvents = _latestKeyPress.asSharedFlow()

    private val _density = MutableStateFlow(Density(1f, appSettings.fontScale))
    val density = _density.asStateFlow()

    private val _ledger = MutableStateFlow(Ledger.Empty)
    val ledger = _ledger.asStateFlow()

    fun setSelectedAsset(value: Asset) {
        _selectedAsset.tryEmit(value)
        appSettings.lastSelectedAsset = value
    }

    fun onKey(key: Key) {
        _latestKeyPress.tryEmit(key)
    }

    fun setLedger(ledger: Ledger) {
        _ledger.tryEmit(ledger)
    }

    fun setDensity(value: Float) {
        val d = _density.value
        _density.tryEmit(Density(d.density, value))
    }

    fun setAppData(appData: AppData) {
        this.appData = appData
        _ledger.tryEmit(appData.ledger)
        appSettings.lastSelectedAsset
            ?.takeIf { appData.ledger.assetsTradings.contains(it) }
            ?.let {
                _selectedAsset.tryEmit(it)
            }
    }
}