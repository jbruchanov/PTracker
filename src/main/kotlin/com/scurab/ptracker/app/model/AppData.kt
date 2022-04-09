package com.scurab.ptracker.app.model

class AppData(
    val ledger: Ledger,
    val prices: Map<Asset, CoinPrice>,
    val historyPrices: Map<Asset, List<PriceItem>>,
    val ledgerStats: LedgerStats
) {
    companion object {
        val Empty = AppData(Ledger.Empty, emptyMap(), emptyMap(), LedgerStats.Empty)
    }
}
