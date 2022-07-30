package com.scurab.ptracker.app.repository

import com.scurab.ptracker.app.model.Asset
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface AppSettings {
    fun flowChanges(emitOnStart: String?): Flow<String>
    var fontScale: Float
    var cryptoCompareApiKey: String?
    var lastSelectedAsset: Asset?
    var latestLedger: String?
    var ledgers: List<String>?
    var primaryCoin: String?
    var debug: Boolean
    var isTradingVolumeVisible: Boolean
    var isTradingAverageVisible: Boolean
    var isTradingTransactionsGroupingEnabled: Boolean

    companion object {
        const val KeyLedgers = "ledgers"
    }
}

@Serializable
class MemoryAppSettings : AppSettings {
    override fun flowChanges(emitOnStart: String?): Flow<String> = throw UnsupportedOperationException()
    override var cryptoCompareApiKey: String? = null
    override var fontScale: Float = 1f
    override var lastSelectedAsset: Asset? = null
    override var latestLedger: String? = null
    override var ledgers: List<String>? = null
    override var primaryCoin: String? = null
    override var debug: Boolean = false
    override var isTradingVolumeVisible: Boolean = true
    override var isTradingAverageVisible: Boolean = true
    override var isTradingTransactionsGroupingEnabled: Boolean = false
}
