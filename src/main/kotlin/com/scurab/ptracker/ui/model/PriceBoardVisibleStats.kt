package com.scurab.ptracker.ui.model

import com.scurab.ptracker.app.ext.ZERO
import com.scurab.ptracker.app.ext.align
import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.isZero
import com.scurab.ptracker.app.ext.safeDiv
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.PriceItem
import java.math.BigDecimal

data class PriceBoardVisibleStats(
    val asset: Asset,
    val avgMarketPrice: BigDecimal,
    val coin1SumBuy: BigDecimal,
    val coin1SumSell: BigDecimal,
    val coin2SumBuy: BigDecimal,
    val coin2SumSell: BigDecimal,
    val volumes: Map<PriceItem, PriceItemVolumes?>,
    val transactions: Int
) {
    private val transactionsBd = transactions.bd
    val coin1Sum = coin1SumBuy + coin1SumSell
    val coin2Sum = coin2SumBuy + coin2SumSell
    val coin1VolumeAbsMax = volumes.maxOfOrNull { it.value?.coin1Volume?.abs() ?: ZERO } ?: ZERO
    val coin2VolumeAbsMax = volumes.maxOfOrNull { it.value?.coin2Volume?.abs() ?: ZERO } ?: ZERO

    val isEmpty = avgMarketPrice.isZero() && transactionsBd.isZero()
    val avgCoin1BuyPrice = coin2SumSell.safeDiv(coin1SumBuy).abs().align
    val avgCoin1SellPrice = coin2SumBuy.safeDiv(coin1SumSell).abs().align
    val avgCoin1TradeDiff = avgCoin1SellPrice - avgCoin1BuyPrice
    val avgCoin1TradeDiffPerc = avgCoin1TradeDiff.safeDiv(avgCoin1BuyPrice).toFloat() * 100f
}