package com.scurab.ptracker.serialisation

import com.scurab.ptracker.net.model.CryptoCompareWsResponse
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CryptoCompareResultSerializer : JsonContentPolymorphicSerializer<CryptoCompareWsResponse>(CryptoCompareWsResponse::class) {
    //https://min-api.cryptocompare.com/documentation/websockets
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["TYPE"]?.jsonPrimitive?.contentOrNull?.toInt() ?: 0) {
        999 -> CryptoCompareWsResponse.HeartBeat.serializer()
        2 -> CryptoCompareWsResponse.MarketTicker.serializer()
        16 -> CryptoCompareWsResponse.SubscriptionAssetDone.serializer()
        in (400..599) -> {
            when (element.jsonObject["MESSAGE"]?.jsonPrimitive?.contentOrNull) {
                "INVALID_SUB" -> CryptoCompareWsResponse.SubscriptionError.serializer()
                else -> CryptoCompareWsResponse.Error.serializer()
            }
        }
        else -> CryptoCompareWsResponse.UnspecificMessage.serializer()
    }
}