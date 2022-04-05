package stub

import com.scurab.ptracker.app.ext.bd
import com.scurab.ptracker.app.ext.marketPercentage
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.CoinPrice
import com.scurab.ptracker.app.model.Holdings
import com.scurab.ptracker.app.model.pieChartData
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
        Holdings(AssetBTCGBP, 0.4.bd, 0.5.bd, 10000.bd),
        Holdings(AssetETHGBP, 3.bd, 3.bd, 10000.bd),
        Holdings(AssetLTCBGP, 40.5.bd, 40.5.bd, 4000.bd),
    )

    fun onlineStubHoldings() = stubHoldings().map { it.realtimeStats(cryptoPrices.getValue(it.asset)) }

    fun pieChartData(): List<PieChartSegment> = onlineStubHoldings()
        .marketPercentage()
        .sortedByDescending { it.asset }
        .pieChartData()
}

