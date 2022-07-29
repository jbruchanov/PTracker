package com.scurab.ptracker.app.model

import androidx.compose.runtime.Composable

class CacheRef<K, V> {
    var key: K? = null; private set
    var item: V? = null; private set

    fun requireValue() = requireNotNull(item)

    fun getOrCreate(key: K, function: () -> V): V {
        if (this.key != key) {
            item = function()
            this.key = key
        }
        return requireValue()
    }

    @Composable
    fun coGetOrCreate(key: K, function: @Composable () -> V): V {
        if (this.key != key) {
            item = function()
            this.key = key
        }
        return requireValue()
    }

    fun clear() {
        key = null
        item = null
    }
}