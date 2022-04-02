package com.scurab.ptracker.repository

import com.scurab.ptracker.model.Asset
import kotlinx.serialization.Serializable

interface AppSettings {
    var fontScale: Float
    var cryptoCompareApiKey: String?
    var lastSelectedAsset: Asset?
    var latestLedger: String?
    var ledgers: List<String>?
}

@Serializable
class MemoryAppSettings : AppSettings {
    override var cryptoCompareApiKey: String? = null
    override var fontScale: Float = 1f
    override var lastSelectedAsset: Asset? = null
    override var latestLedger: String? = null
    override var ledgers: List<String>? = null
}
