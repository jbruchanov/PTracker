package com.scurab.ptracker.app.model

import com.scurab.ptracker.app.ext.align
import com.scurab.ptracker.app.ext.inverse
import com.scurab.ptracker.app.serialisation.BigDecimalAsStringSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class CoinPrice(
    @SerialName("asset") override val asset: Asset,
    @SerialName("price") @Serializable(with = BigDecimalAsStringSerializer::class) override val price: BigDecimal
) : MarketPrice {

    fun convertCurrency(marketPrice: MarketPrice, currency: String = marketPrice.asset.coin1): CoinPrice {
        if (asset.coin2 == currency) return this
        require(asset.contains(marketPrice.asset.coin1, marketPrice.asset.coin2)) { "Invalid data, asset:${asset}, marketPrice:$marketPrice" }
        require(marketPrice.asset.has(currency)) { "Invalid market price:${marketPrice}, doesn't have currency:$currency" }

        val (coin, price) = when {
            asset.coin2 == marketPrice.asset.coin1 -> marketPrice.asset.coin2 to price * marketPrice.price
            asset.coin2 == marketPrice.asset.coin2 -> marketPrice.asset.coin1 to price * marketPrice.price.inverse()
            else -> throw IllegalStateException("Unsupported combination, asset:${asset}, marketPrice:${marketPrice}")
        }
        return CoinPrice(Asset(asset.coin1, coin), price.align)
    }

    override fun flipAsset() = CoinPrice(asset.flipCoins(), price.inverse())

    companion object {
        fun MarketPrice.asCoinPrice() = if (this is CoinPrice) this else CoinPrice(asset, price)

        fun fromUnknownPairOrNull(coin1: String?, coin2: String?, price: BigDecimal): CoinPrice? {
            val asset = Asset.fromUnknownPairOrNull(coin1, coin2) ?: return null
            val price = if(asset.coin1 == coin1) price else price.inverse()
            return CoinPrice(asset, price)
        }
    }
}