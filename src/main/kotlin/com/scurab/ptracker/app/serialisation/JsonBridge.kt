package com.scurab.ptracker.app.serialisation

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object JsonBridge {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        serializersModule = SerializersModule {

        }
    }

    val jsonBeautify = Json(from = json) {
        this.prettyPrint = true
    }

    inline fun <reified T> serialize(any: T, beautify: Boolean = false) = (if (beautify) jsonBeautify else json).encodeToString(any)

    inline fun <reified T> deserialize(string: String) = json.decodeFromString<T>(string)

}
