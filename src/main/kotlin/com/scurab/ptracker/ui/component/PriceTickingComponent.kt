package com.scurab.ptracker.ui.component

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.MarketPrice
import com.scurab.ptracker.app.repository.PricesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


interface PriceTickingComponent {

    fun CoroutineScope.startPriceObserver(prices: SnapshotStateMap<Asset, MarketPrice>)

    class Default(private val pricesRepository: PricesRepository) : PriceTickingComponent {

        override fun CoroutineScope.startPriceObserver(prices: SnapshotStateMap<Asset, MarketPrice>) {
            launch(Dispatchers.Main) {
                prices.putAll(pricesRepository.latestPrices)
                pricesRepository.wsMarketPrice
                    .collect { marketPrice ->
                        prices[marketPrice.asset] = marketPrice
                    }
            }
        }
    }
}