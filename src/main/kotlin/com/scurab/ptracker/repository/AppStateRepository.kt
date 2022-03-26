package com.scurab.ptracker.repository

import com.scurab.ptracker.model.Asset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository {

    private val _selectedAsset = MutableStateFlow(Asset("BTC", "GBP"))
    val selectedAsset = _selectedAsset.asStateFlow()

    fun setSelectedAsset(value: Asset) {
        _selectedAsset.tryEmit(value)
    }
}