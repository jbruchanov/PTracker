package com.scurab.ptracker.serialisation

import com.scurab.ptracker.net.model.CryptoCompareWssResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

object JsonBridge {

    val json: Json = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(baseClass = CryptoCompareWssResponse::class) {
                subclass(CryptoCompareWssResponse.MarketTicker::class)
                defaultDeserializer { CryptoCompareWssResponse.UnspecificMessage.serializer() }
            }
        }
    }

    val jsonBeautify = Json(from = json) {
        this.prettyPrint = true
    }

    inline fun <reified T> serialize(any: T, beautify: Boolean = false) = (if (beautify) jsonBeautify else json).encodeToString(any)

    inline fun <reified T> deserialize(string: String) = json.decodeFromString<T>(string)

}
