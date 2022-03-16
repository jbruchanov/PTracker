package com.scurab.ptracker.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppStateRepository {

    private val _selectedAsset = MutableStateFlow("BTC-GBP")
    val selectedAsset = _selectedAsset.asStateFlow()

    fun setSelectedAsset(value: String) {
        _selectedAsset.tryEmit(value)
    }
}