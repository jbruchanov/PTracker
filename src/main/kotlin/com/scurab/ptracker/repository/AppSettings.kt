package com.scurab.ptracker.repository

import kotlinx.serialization.Serializable

interface AppSettings {
    var cryptoCompareApiKey: String?
}

@Serializable
class MemoryAppSettings : AppSettings {
    override var cryptoCompareApiKey: String? = null
}