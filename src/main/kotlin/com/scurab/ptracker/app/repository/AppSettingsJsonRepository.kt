package com.scurab.ptracker.app.repository

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.Locations
import com.scurab.ptracker.app.serialisation.JsonBridge
import com.scurab.ptracker.component.ProcessScope
import com.scurab.ptracker.component.delegate.OnKeyChangeListener
import com.scurab.ptracker.component.delegate.WithNotifyingMutableProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.io.File

//expected to be singleton, otherwise memleak in saving coroutine
class AppSettingsJsonRepository(
    private val data: MemoryAppSettings = MemoryAppSettings(),
    private val jsonBridge: JsonBridge,
    private val autoSave: Boolean = true
) : AppSettings, WithNotifyingMutableProperties by WithNotifyingMutableProperties.Delegate() {
    //extra buffer & replay to catch all events even if subscribed after
    private val flow = MutableSharedFlow<String>(extraBufferCapacity = 16, replay = 16)

    override fun flowChanges(emitOnStart: String?): Flow<String> {
        return flow.onStart { if (emitOnStart != null) emit(emitOnStart) else Unit }
    }

    private val onChangeListener: OnKeyChangeListener = { flow.tryEmit(it) }

    init {
        setNotifyingCallback(onChangeListener)
        if (autoSave) {
            ProcessScope.launch(Dispatchers.IO) {
                flow.debounce(1000L).collect { save() }
            }
        }
    }

    fun save() {
        File(Locations.Settings).writeText(jsonBridge.serialize(data, beautify = true))
    }

    override var cryptoCompareApiKey: String? by data::cryptoCompareApiKey.notifying()
    override var fontScale: Float by data::fontScale.notifying()
    override var lastSelectedAsset: Asset? by data::lastSelectedAsset.notifying()
    override var latestLedger: String? by data::latestLedger.notifying()
    override var ledgers: List<String>? by data::ledgers.notifying()
    override var primaryCoin by data::primaryCoin.notifying()
    override var debug by data::debug.notifying()

    companion object {

        fun default(jsonBridge: JsonBridge): AppSettingsJsonRepository {
            val file = File(Locations.Settings)
            val data = kotlin.runCatching { jsonBridge.deserialize<MemoryAppSettings>(file.readText()) }.getOrNull() ?: MemoryAppSettings()
            return AppSettingsJsonRepository(data, jsonBridge, autoSave = true)
        }
    }
}


