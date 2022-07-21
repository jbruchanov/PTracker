package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.align
import com.scurab.ptracker.app.ext.isNotZero
import com.scurab.ptracker.app.ext.round
import com.scurab.ptracker.app.ext.sameElseSwap
import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import java.util.UUID

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
    abstract val id: Int
    abstract val exchange: String
    abstract val type: String
    abstract override val dateTime: LocalDateTime
    abstract val feeQuantity: BigDecimal
    abstract val feeAsset: String
    abstract val feeValueInFiat: BigDecimal?
    abstract val wallet: String
    abstract val note: String?

    abstract fun hasOrIsRelatedAsset(asset: Asset): Boolean
    abstract fun hasAsset(asset: Asset): Boolean
    abstract val assetsLabel: String
    abstract val assets: Set<String>

    val uuid = UUID.randomUUID()
    var priceItem: PriceItem? by cache
    var originalTransaction: Transaction? = null

    data class Income(
        override val id: Int,
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
        override fun hasOrIsRelatedAsset(asset: Asset): Boolean = asset.has(buyAsset)
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset)
        override val assets: Set<String> = setOf(buyAsset)
    }

    data class Outcome(
        override val id: Int,
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
        override fun hasOrIsRelatedAsset(asset: Asset): Boolean = asset.has(sellAsset)
        override fun hasAsset(asset: Asset): Boolean = asset.has(sellAsset)
        override val assets: Set<String> = setOf(sellAsset)
    }

    data class Trade(
        override val id: Int,
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
        override fun hasOrIsRelatedAsset(asset: Asset): Boolean = hasAsset(asset) || isRelatedAsset(asset)
        override fun hasAsset(asset: Asset): Boolean = asset.has(buyAsset, sellAsset)
        private fun isRelatedAsset(asset: Asset) = (sellAsset.isEmpty() && asset.has(buyAsset)) || (buyAsset.isEmpty() && asset.has(sellAsset))

        override val assetsLabel: String = asset.label

        override val assets: Set<String> = setOf(buyAsset, sellAsset)
    }

    fun isTransactionWithAsset(asset: Asset) = this is Trade && hasOrIsRelatedAsset(asset)
    fun isTransactionWithCoin(coin: String) = this is Trade && hasCoin(coin)

    fun hasCoin(coin: String) = (this is HasIncome && this.buyAsset == coin) || (this is HasOutcome && this.sellAsset == coin)

    val isCryptoBuy by lazy { this is Trade && !FiatCurrencies.contains(buyAsset) && FiatCurrencies.contains(sellAsset) }
    val isCryptoSell by lazy { this is Trade && FiatCurrencies.contains(buyAsset) && !FiatCurrencies.contains(sellAsset) }
    val isCryptoTrade by lazy { isCryptoBuy || isCryptoSell }
    val isCryptoDeposit by lazy { this is HasIncome && type == TypeDeposit && !FiatCurrencies.contains(buyAsset) }
    val isCryptoWithdrawal by lazy { this is HasOutcome && type == TypeWithdrawal && !FiatCurrencies.contains(sellAsset) }
    val isCryptoExchange by lazy { this is Trade && !FiatCurrencies.contains(buyAsset) && !FiatCurrencies.contains(sellAsset) }
    val isFiatExchange by lazy { this is Trade && FiatCurrencies.contains(buyAsset) && FiatCurrencies.contains(sellAsset) }

    fun unitPrice(): BigDecimal? {
        if (this !is Trade) return null
        val (fiat, crypto) = Pair(buyQuantity, sellQuantity).sameElseSwap(FiatCurrencies.contains(buyAsset))
        return (fiat.align / crypto).round(true)
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

    fun coinValues(): List<CoinValue> {
        val income = if (this is HasIncome /*&& !FiatCurrencies.contains(buyAsset) && buyQuantity.isNotZero()*/) CoinValue(buyAsset, buyQuantity) else null
        val outcome = if (this is HasOutcome /*&& !FiatCurrencies.contains(sellAsset) && sellQuantity.isNotZero()*/) CoinValue(sellAsset, -sellQuantity) else null
        val fee = if (feeQuantity.isNotZero()/* && !FiatCurrencies.contains(feeAsset)*/) CoinValue(feeAsset, -feeQuantity) else null
        return listOfNotNull(income, outcome, fee).groupBy { it.coin }.map { (coin, quantities) -> CoinValue(coin, quantities.sumOf { it.quantity }) }
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
        val _TypeCryptoExchange = "CryptoExchange"
        val _TypeFiatExchange = "FiatExchange"

        private val UnImportantType = setOf(
            TypeDeposit, TypeWithdrawal
        )
    }
}
