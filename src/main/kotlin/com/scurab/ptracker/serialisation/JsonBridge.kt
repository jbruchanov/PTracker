package com.scurab.ptracker.serialisation

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonBridge {

    val json: Json = Json {
        ignoreUnknownKeys = true
    }

    inline fun <reified T> serialize(any: T) = json.encodeToString(any)

    inline fun <reified T> deserialize(string: String) = json.decodeFromString<T>(string)
}
