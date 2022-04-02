package com.scurab.ptracker.serialisation

import com.scurab.ptracker.net.model.CryptoCompareWssResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object CryptoCompareResultSerializer : JsonContentPolymorphicSerializer<CryptoCompareWssResponse>(CryptoCompareWssResponse::class) {
    //https://min-api.cryptocompare.com/documentation/websockets
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["TYPE"]?.jsonPrimitive?.contentOrNull?.toInt() ?: 0) {
        999 -> CryptoCompareWssResponse.HeartBeat.serializer()
        2 -> CryptoCompareWssResponse.MarketTicker.serializer()
        16 -> CryptoCompareWssResponse.SubscriptionAssetDone.serializer()
        in (400..599) -> {
            when (element.jsonObject["MESSAGE"]?.jsonPrimitive?.contentOrNull) {
                "INVALID_SUB" -> CryptoCompareWssResponse.SubscriptionError.serializer()
                else -> CryptoCompareWssResponse.Error.serializer()
            }
        }
        else -> CryptoCompareWssResponse.UnspecificMessage.serializer()
    }
}