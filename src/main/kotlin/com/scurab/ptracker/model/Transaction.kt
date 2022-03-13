package com.scurab.ptracker.model

import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import kotlin.random.Random

sealed class Transaction {
    abstract val type: String
    abstract val time: LocalDateTime
    abstract val feeQuantity: BigDecimal
    abstract val feeAsset: String
    abstract val feeValueInFiat: BigDecimal?
    abstract val wallet: String
    abstract val note: String?

    data class Income(
        override val type: String,
        override val time: LocalDateTime,
        val buyQuantity: BigDecimal,
        val buyAsset: String,
        val buyValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction()

    data class Outcome(
        override val type: String,
        override val time: LocalDateTime,
        val sellQuantity: BigDecimal,
        val sellAsset: String,
        val sellValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction()

    data class Trade(
        override val type: String,
        override val time: LocalDateTime,
        val buyQuantity: BigDecimal,
        val buyAsset: String,
        val buyValueInFiat: BigDecimal?,
        val sellQuantity: BigDecimal,
        val sellAsset: String,
        val sellValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction()
}
