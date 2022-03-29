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

sealed class Transaction(private val cache: MutableMap<String, Any?> = mutableMapOf()) :
    HasDateTime, WithCache by MapCache(cache) {
    abstract val exchange: String
    abstract val type: String
    abstract override val dateTime: LocalDateTime
    abstract val feeQuantity: BigDecimal
    abstract val feeAsset: String
    abstract val feeValueInFiat: BigDecimal?
    abstract val wallet: String
    abstract val note: String?

    abstract fun hasAsset(asset: Asset): Boolean
    abstract val assetsLabel: String
    abstract val assets: Set<String>

    var priceItem: PriceItem? by cache

    class Income(
        override val exchange: String,
        override val type: String,
        override val dateTime: LocalDateTime,
        override val buyQuantity: BigDecimal,
        override val buyAsset: String,
        val buyValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction(), HasIncome {
        override val assetsLabel: String = buyAsset
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset)
        override val assets: Set<String> = setOf(buyAsset)
    }

    data class Outcome(
        override val exchange: String,
        override val type: String,
        override val dateTime: LocalDateTime,
        override val sellQuantity: BigDecimal,
        override val sellAsset: String,
        val sellValueInFiat: BigDecimal?,
        override val feeQuantity: BigDecimal,
        override val feeAsset: String,
        override val feeValueInFiat: BigDecimal?,
        override val wallet: String,
        override val note: String?,
    ) : Transaction(), HasOutcome {
        override val assetsLabel: String = sellAsset
        override fun hasAsset(asset: Asset): Boolean = asset.has(sellAsset)
        override val assets: Set<String> = setOf(sellAsset)
    }

    data class Trade(
        override val exchange: String,
        override val type: String,
        override val dateTime: LocalDateTime,
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
        override val assetsLabel: String = asset.text
        fun isCryptoBuy() = FiatCurrencies.contains(sellAsset)
        override val assets: Set<String> = setOf(buyAsset, sellAsset)
    }

    fun isTransactionWithAsset(asset: Asset) = this is Trade && hasAsset(asset)

    fun unitPrice(): BigDecimal? {
        if (this !is Trade) return null
        val (fiat, crypto) = Pair(buyQuantity, sellQuantity).sameElseSwap(FiatCurrencies.contains(buyAsset))
        return (fiat / crypto).round(true)
    }

    private val debugString by lazy {
        buildString {
            append("Transaction(dateTime=$dateTime")
            (this@Transaction as? HasIncome)?.apply {
                append(",${buyQuantity.toPlainString()} $buyAsset")
            }
            (this@Transaction as? HasOutcome)?.apply {
                append(",${sellQuantity.toPlainString()} $sellQuantity")
            }
            append(",${feeQuantity.toPlainString()} $feeAsset")
            append(", wallet='$wallet', exchange='$exchange', type='$type')")
        }
    }

    val isImportant by lazy { !UnImportantType.contains(type) }

    override fun toString(): String = debugString

    companion object {
        val TypeDeposit = "Deposit"
        val TypeWithdrawal = "Withdrawal"
        val TypeAirdrop = "Airdrop"
        val TypeMining = "Mining"
        val TypeStaking = "Staking"
        val TypeInterest = "Interest"
        val TypeDividend = "Dividend"
        val TypeIncome = "Income"
        val TypeGiftReceived = "Gift-Received"
        val TypeGiftSent = "Gift-Sent"
        val TypeCharitySent = "Charity-Sent"
        val TypeGiftSpouse = "Gift-Spouse"
        val TypeLost = "Lost"
        val TypeTrade = "Trade"
        val _TypeTradeIn = "TradeIn"
        val _TypeTradeOut = "TradeOut"

        private val UnImportantType = setOf(
            TypeDeposit, TypeWithdrawal
        )
    }
}
