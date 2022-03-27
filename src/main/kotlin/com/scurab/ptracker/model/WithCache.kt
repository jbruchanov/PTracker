package com.scurab.ptracker.model

interface WithCache {
    fun <T : Any> getOrPut(key: String, defaultValue: () -> T): T
}

class MapCache(val cache: MutableMap<String, Any?> = mutableMapOf()) : WithCache {
    override fun <T : Any> getOrPut(key: String, defaultValue: () -> T): T {
        if (!cache.containsKey(key)) {
            cache[key] = defaultValue()
        }
        return cache[key] as T
    }
}