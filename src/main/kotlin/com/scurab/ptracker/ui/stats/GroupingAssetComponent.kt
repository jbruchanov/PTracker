package com.scurab.ptracker.ui.stats

import com.scurab.ptracker.app.model.AppData
import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.DataFilter
import com.scurab.ptracker.app.model.DateGrouping
import com.scurab.ptracker.app.model.Tuple4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

interface GroupingAssetComponent {

    fun Flow<AppData>.combineWithGroupingAsset(): Flow<Tuple4<AppData, DateGrouping, Asset?, DataFilter>>
    fun GroupingAssetComponent.tryEmitGrouping(grouping: DateGrouping) = Unit
    fun GroupingAssetComponent.tryEmitAsset(asset: Asset?) = Unit
    fun GroupingAssetComponent.tryEmitFilter(dataFilter: DataFilter) = Unit

    class Default(grouping: DateGrouping, asset: Asset?, filter: DataFilter = DataFilter.All) : GroupingAssetComponent {
        private val flowGrouping = MutableStateFlow(grouping)
        private val flowAsset = MutableStateFlow(asset)
        private val flowFilter = MutableStateFlow(filter)

        override fun Flow<AppData>.combineWithGroupingAsset(): Flow<Tuple4<AppData, DateGrouping, Asset?, DataFilter>> {
            return combine(this, flowGrouping, flowAsset, flowFilter) { appData, grouping, asset, filter ->
                val asset = asset?.takeIf {
                    appData.ledger.assetsTradings.contains(it) ||
                        (it.isSingleCoinAsset && appData.ledger.coins.contains(it.coin1))
                }
                Tuple4(appData, grouping, asset, filter)
            }
        }

        override fun GroupingAssetComponent.tryEmitGrouping(grouping: DateGrouping) {
            flowGrouping.tryEmit(grouping)
        }

        override fun GroupingAssetComponent.tryEmitAsset(asset: Asset?) {
            flowAsset.tryEmit(asset)
        }

        override fun GroupingAssetComponent.tryEmitFilter(dataFilter: DataFilter) {
            flowFilter.tryEmit(dataFilter)
        }
    }
}
