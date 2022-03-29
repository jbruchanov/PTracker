@file:UseSerializers(BigDecimalSerializer::class)

package com.scurab.ptracker.net.model

import com.scurab.ptracker.json.BigDecimalSerializer
import com.scurab.ptracker.model.IPriceItem
import com.scurab.ptracker.model.MapCache
import com.scurab.ptracker.model.WithCache
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

@Serializable
data class CryptoCoinDetail(
    @SerialName("Id") val id: String,
    @SerialName("ImageUrl") val imageUrl: String,
    @SerialName("CoinName") val coinName: String,
    @SerialName("Symbol") val symbol: String,
    @SerialName("TotalCoinsMined") val totalCoinsMined: BigDecimal,
    @SerialName("CirculatingSupply") val circulatingSupply: BigDecimal,
    @SerialName("AssetLaunchDate") val assetLaunchDate: String,
) {
    val fullImageUrl by lazy { "https://www.cryptocompare.com$imageUrl" }
}