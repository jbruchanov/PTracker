package stub

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.marketPercentage
import com.scurab.ptracker.app.ext.pieChartData
import com.scurab.ptracker.app.model.AnyCoin
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinExchangeStats
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.CryptoHoldings
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.ui.common.PieChartSegment

object StubData {
    val AssetBTCGBP = Asset("BTC", "GBP")
    val AssetLTCBGP = Asset("LTC", "GBP")
    val AssetETHGBP = Asset("ETH", "GBP")
    val AssetETHUSD = Asset("ETH", "USD")

    val PriceBTCGBP = CoinPrice(AssetBTCGBP, 35000.bd)
    val PriceETHGBP = CoinPrice(AssetETHGBP, 2600.bd)
    val PriceETHUSD = CoinPrice(AssetETHUSD, 3500.bd)
    val PriceLTCGBP = CoinPrice(AssetLTCBGP, 94.25.bd)


    val cryptoTradingAssets = listOf(AssetBTCGBP, AssetLTCBGP, AssetETHUSD)
    val cryptoPrices = listOf(PriceBTCGBP, PriceETHGBP, PriceETHUSD, PriceLTCGBP).associateBy { it.asset }

    fun stubHoldings() = listOf(
        CryptoHoldings(AssetBTCGBP, 0.4.bd, 0.5.bd, 10000.bd, 0.001.bd),
        CryptoHoldings(AssetETHGBP, 3.bd, 3.bd, 10000.bd, 0.02.bd),
        CryptoHoldings(AssetLTCBGP, 40.5.bd, 40.5.bd, 4000.bd, 0.bd),
    )

    fun coinExchangeStats() = listOf(
        CoinExchangeStats(AnyCoin("GBP"), ExchangeWallet("Blainance"), 100.bd, 0.5.bd, null),
        CoinExchangeStats(AnyCoin("BTC"), ExchangeWallet("Wallet"), 0.000005.bd, 0.001.bd, null),
        CoinExchangeStats(AnyCoin("ETH"), ExchangeWallet("Quippo"), 1.0.bd, 0.01.bd, null),
        CoinExchangeStats(AnyCoin("ADA"), ExchangeWallet("AdaLite"), 1000.0.bd, 0.5.bd, null),
    )

    fun onlineStubHoldings() = stubHoldings().map { it.realtimeStats(cryptoPrices.getValue(it.asset)) }

    fun pieChartData(): List<PieChartSegment> = onlineStubHoldings()
        .marketPercentage()
        .sortedByDescending { it.asset }
        .pieChartData()
}

