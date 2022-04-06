package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.round
import com.scurab.ptracker.app.ext.sameElseSwap
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

    data class Income(
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
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset, sellAsset) || isRelatedAsset(asset)
        private fun isRelatedAsset(asset: Asset) = (sellAsset.isEmpty() && asset.has(buyAsset)) || (buyAsset.isEmpty() && asset.has(sellAsset))
        override val assetsLabel: String = asset.label

        override val assets: Set<String> = setOf(buyAsset, sellAsset)
    }

    fun isTransactionWithAsset(asset: Asset) = this is Trade && hasAsset(asset)

    fun hasCoin(coin: String) = (this is HasIncome && this.buyAsset == coin) || (this is Outcome && this.sellAsset == coin)

    val isCryptoBuy by lazy { (this as? Trade)?.let { FiatCurrencies.contains(sellAsset) } ?: false }
    val isCryptoSell by lazy { (this as? Trade)?.let { !FiatCurrencies.contains(sellAsset) } ?: false }
    val isCryptoDeposit by lazy { (this as? HasIncome)?.let { type == TypeDeposit && !FiatCurrencies.contains(it.buyAsset) } ?: false }
    val isCryptoWithdrawal by lazy { (this as? HasOutcome)?.let { type == TypeWithdrawal && !FiatCurrencies.contains(it.sellAsset) } ?: false }

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

    val isImportant by lazy { isCryptoWithdrawal || isCryptoDeposit || !UnImportantType.contains(type) }

    val asset by lazy {
        val buyAsset = (this as? HasIncome)?.buyAsset ?: ""
        val sellAsset = (this as? HasOutcome)?.sellAsset ?: ""
        val isBuyAssetFiat = FiatCurrencies.contains(buyAsset)
        val fiat = if (isBuyAssetFiat) buyAsset else sellAsset
        val crypto = if (!isBuyAssetFiat) buyAsset else sellAsset
        require(fiat.isNotEmpty() || crypto.isNotEmpty()) {
            "$this, fiat:$fiat, crypto:$crypto, both are empty"
        }
        Asset(crypto, fiat)
    }

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
        val _TypeCryptoDeposit = "CryptoDeposit"
        val _TypeCryptoWithdrawal = "CryptoWithdrawal"

        private val UnImportantType = setOf(
            TypeDeposit, TypeWithdrawal
        )
    }
}
