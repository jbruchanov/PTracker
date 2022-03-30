package com.scurab.ptracker.serialisation

import com.scurab.ptracker.net.model.CryptoCompareWssResponse
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CryptoCompareResultSerializer : JsonContentPolymorphicSerializer<CryptoCompareWssResponse>(CryptoCompareWssResponse::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["TYPE"]?.jsonPrimitive?.contentOrNull) {
        "2" -> CryptoCompareWssResponse.MarketTicker.serializer()
        else -> CryptoCompareWssResponse.UnspecificMessage.serializer()
    }
}