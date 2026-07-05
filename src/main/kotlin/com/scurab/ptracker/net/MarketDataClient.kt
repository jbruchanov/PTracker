package com.scurab.ptracker.net

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.FiatCoin
import com.scurab.ptracker.app.model.WsExchangeResponse
import com.scurab.ptracker.net.model.CryptoComparePriceItem
import com.scurab.ptracker.net.model.CryptoCompareWssSubscriptionArg
import com.scurab.ptracker.ui.model.Validity
import kotlinx.coroutines.channels.Channel

interface MarketDataClient {

    suspend fun testKey(key: String): Validity

    suspend fun getDailyHistory(cryptoSymbol: String, fiatSymbol: String, limit: Int = 1000): List<CryptoComparePriceItem>

    suspend fun getCoinIconUrl(coin: String): String?

    suspend fun getPrices(assets: Collection<Asset>, primaryFiatCoin: FiatCoin? = null): List<CoinPrice>

    fun subscribeTicker(args: List<CryptoCompareWssSubscriptionArg>): Channel<out WsExchangeResponse>

    fun stop()
}
