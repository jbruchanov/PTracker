package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.Tuple3
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

interface GroupingAssetComponent {

    fun Flow<AppData>.combineWithGroupingAsset(): Flow<Tuple3<AppData, DateGrouping, Asset?>>
    fun GroupingAssetComponent.tryEmitGrouping(grouping: DateGrouping) = Unit
    fun GroupingAssetComponent.tryEmitAsset(asset: Asset?) = Unit

    class Default(grouping: DateGrouping, asset: Asset?) : GroupingAssetComponent {
        private val flowGrouping = MutableStateFlow(grouping)
        private val flowAsset = MutableStateFlow(asset)

        override fun Flow<AppData>.combineWithGroupingAsset(): Flow<Tuple3<AppData, DateGrouping, Asset?>> {
            return combine(flowGrouping, ::Pair)
                .combine(flowAsset) { appDataAndGrouping, selectedAsset ->
                    val (appData, grouping) = appDataAndGrouping
                    val asset = selectedAsset?.takeIf {
                        appData.ledger.assetsTradings.contains(it) ||
                            (it.isSingleCoinAsset && appData.ledger.coins.contains(it.coin1))
                    }
                    Tuple3(appData, grouping, asset)
                }
        }

        override fun GroupingAssetComponent.tryEmitGrouping(grouping: DateGrouping) {
            flowGrouping.tryEmit(grouping)
        }

        override fun GroupingAssetComponent.tryEmitAsset(asset: Asset?) {
            flowAsset.tryEmit(asset)
        }
    }
}
