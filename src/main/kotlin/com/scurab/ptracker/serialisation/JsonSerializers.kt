package com.scurab.ptracker.serialisation

import com.scurab.ptracker.ext.align
import com.scurab.ptracker.ext.toLong
import com.scurab.ptracker.model.Asset
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

object BigDecimalAsStringSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.math.BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        return kotlin
            .runCatching { BigDecimal(decoder.decodeDouble()).align }
            .getOrElse { BigDecimal(decoder.decodeString()).align }
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }
}

object BigDecimalAsDoubleSerializer : KSerializer<BigDecimal> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.math.BigDecimal", PrimitiveKind.DOUBLE)

    override fun deserialize(decoder: Decoder): BigDecimal {
        return kotlin
            .runCatching { BigDecimal(decoder.decodeDouble()).align }
            .getOrElse { BigDecimal(decoder.decodeString()).align }
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeDouble(value.toDouble())
    }
}

object MillisLongAsDateTimeSerializer : KSerializer<LocalDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlinx.datetime.LocalDateTime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return decoder.decodeLong().let {
            Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeLong(value.toLong())
    }
}

object SecondsLongAsDateTimeSerializer : KSerializer<LocalDateTime> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("kotlinx.datetime.LocalDateTime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return decoder.decodeLong().let {
            Instant.fromEpochMilliseconds(it * 1000).toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeLong(value.toLong() / 1000)
    }
}

object AssetAsStringSerializer : KSerializer<Asset> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.scurab.ptracker.model.Asset", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Asset {
        return Asset.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Asset) {
        encoder.encodeString(value.label)
    }
}