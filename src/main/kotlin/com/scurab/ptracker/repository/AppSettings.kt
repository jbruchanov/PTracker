package com.scurab.ptracker.repository

import kotlinx.serialization.Serializable

interface AppSettings {
    var fontScale: Float
    var cryptoCompareApiKey: String?
}

@Serializable
class MemoryAppSettings : AppSettings {
    override var cryptoCompareApiKey: String? = null
    override var fontScale: Float = 1f
}
