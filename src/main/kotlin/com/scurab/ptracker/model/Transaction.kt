package com.scurab.ptracker.model

import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

sealed class Transaction {
    abstract val exchange: String
    abstract val type: String
    abstract val time: LocalDateTime
    abstract val feeQuantity: BigDecimal
    abstract val feeAsset: String
    abstract val feeValueInFiat: BigDecimal?
    abstract val wallet: String
    abstract val note: String?

    abstract fun hasAsset(asset: Asset): Boolean

    data class Income(
        override val exchange: String,
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
    ) : Transaction() {
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset)
    }

    data class Outcome(
        override val exchange: String,
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
    ) : Transaction() {
        override fun hasAsset(asset: Asset): Boolean = asset.has(sellAsset)
    }

    data class Trade(
        override val exchange: String,
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
    ) : Transaction() {
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset, sellAsset)
        val asset by lazy {
            val isBuyAssetFiat = FiatCurrencies.contains(buyAsset)
            val fiat = if (isBuyAssetFiat) buyAsset else sellAsset
            val crypto = if (!isBuyAssetFiat) buyAsset else sellAsset
            Asset(crypto, fiat)
        }
    }

    fun isTransactionWithAsset(asset: Asset) = this is Trade && hasAsset(asset)
}
