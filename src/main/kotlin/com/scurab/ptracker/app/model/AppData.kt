package com.scurab.ptracker.app.model

class AppData(
    val ledger: Ledger,
    val prices: List<CoinPrice>,
    val historyPrices: Map<Asset, List<PriceItem>>,
    val ledgerStats: LedgerStats
)
