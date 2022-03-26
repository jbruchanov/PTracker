package com.scurab.ptracker.model

import com.scurab.ptracker.ext.round
import com.scurab.ptracker.ext.sameElseSwap
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal

interface HasIncome {
    val buyQuantity: BigDecimal
    val buyAsset: String
}

interface HasOutcome {
    val sellQuantity: BigDecimal
    val sellAsset: String
}

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
    abstract val assets: String

    data class Income(
        override val exchange: String,
        override val type: String,
        override val time: LocalDateTime,
        override val buyQuantity: BigDecimal,
        override val buyAsset: String,
        val buyValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction(), HasIncome {
        override val assets: String = buyAsset
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset)
    }

    data class Outcome(
        override val exchange: String,
        override val type: String,
        override val time: LocalDateTime,
        override val sellQuantity: BigDecimal,
        override val sellAsset: String,
        val sellValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction(), HasOutcome {
        override val assets: String = sellAsset
        override fun hasAsset(asset: Asset): Boolean = asset.has(sellAsset)
    }

    data class Trade(
        override val exchange: String,
        override val type: String,
        override val time: LocalDateTime,
        override val buyQuantity: BigDecimal,
        override val buyAsset: String,
        val buyValueInFiat: BigDecimal?,
        override val sellQuantity: BigDecimal,
        override val sellAsset: String,
        val sellValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction(), HasIncome, HasOutcome {
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset, sellAsset)
        val asset by lazy {
            val isBuyAssetFiat = FiatCurrencies.contains(buyAsset)
            val fiat = if (isBuyAssetFiat) buyAsset else sellAsset
            val crypto = if (!isBuyAssetFiat) buyAsset else sellAsset
            Asset(crypto, fiat)
        }
        override val assets: String = asset.text
    }

    fun isTransactionWithAsset(asset: Asset) = this is Trade && hasAsset(asset)

    fun unitPrice(): BigDecimal? {
        if (this !is Trade) return null
        val (fiat, crypto) = Pair(buyQuantity, sellQuantity).sameElseSwap(FiatCurrencies.contains(buyAsset))
        return (fiat / crypto).round(true)
    }
}
