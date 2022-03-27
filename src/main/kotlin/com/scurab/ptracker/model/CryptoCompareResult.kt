@file:UseSerializers(BigDecimalSerializer::class)

package com.scurab.ptracker.model

import com.scurab.ptracker.json.BigDecimalSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.math.BigDecimal

@Serializable()
class CryptoCompareResult<T>(
    @SerialName("Response") val response: String,
    @SerialName("Message") val message: String,
    @SerialName("HasWarning") val hasWarning: Boolean,
    @SerialName("Type") val type: Int,
    @SerialName("Data") val data: T,
)

@Serializable
class CryptoCompareHistoryData(
    @SerialName("Aggregated") val aggregated: Boolean,
    @SerialName("TimeFrom") val timeFrom: Long,
    @SerialName("TimeTo") val timeTo: Long,
    @SerialName("Data") val items: List<CryptoComparePriceItem>
)

@Serializable
data class CryptoComparePriceItem(
    @SerialName("time") val time: Long,
    @SerialName("high") override val high: BigDecimal,
    @SerialName("low") override val low: BigDecimal,
    @SerialName("open") override val open: BigDecimal,
    @SerialName("close") override val close: BigDecimal,
    @SerialName("volumefrom") val volumeFrom: BigDecimal,
    @SerialName("volumeto") val volumeTo: BigDecimal,
    @SerialName("conversionType") val conversionType: String?,
    @SerialName("conversionSymbol") val conversionSymbol: String?
) : IPriceItem, WithCache by MapCache() {
    val localDateTime by lazy(LazyThreadSafetyMode.NONE) { Instant.fromEpochMilliseconds(time * 1000).toLocalDateTime(TimeZone.currentSystemDefault()) }
    val localDate by lazy(LazyThreadSafetyMode.NONE) { localDateTime.date }
    override val dateTime: LocalDateTime = localDateTime
}