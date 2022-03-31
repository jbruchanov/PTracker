package com.scurab.ptracker.repository

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Density
import com.scurab.ptracker.model.Asset
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository(appSettings: AppSettings) {

    private val _selectedAsset = MutableStateFlow(Asset("BTC", "GBP"))
    val selectedAsset = _selectedAsset.asStateFlow()

    private val _latestKeyPress = MutableSharedFlow<Key>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val keyEvents = _latestKeyPress.asSharedFlow()

    private val _density = MutableStateFlow(Density(1f, appSettings.fontScale))
    val density = _density.asStateFlow()

    fun setSelectedAsset(value: Asset) {
        _selectedAsset.tryEmit(value)
    }

    fun onKey(key: Key) {
        _latestKeyPress.tryEmit(key)
    }
}