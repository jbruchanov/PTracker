package com.scurab.ptracker.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy

val LocalTexts = compositionLocalOf(structuralEqualityPolicy()) { English }

interface Texts {
    val Fee: String
    val Sell: String
    val Buy: String
    val AppTitle: String
    val Settings: String
    val FontScaling: String
    val CryptoCompareApiKey: String
    val Test: String
    val Holdings: String
    val Balance: String
    val Cost: String
    val Price: String
    val MarketValue: String
    val ROI: String
    val Asset: String
    val Save: String
    val Ledgers: String
    val RestartNeeded: String
    val Unit: String
    val Stats: String
    val TradingStats: String
    val PrimaryCoin: String
    val ExchangeWallet: String
    val NoProfitableOutcome: String
    val FreeIncome: String
    val TotalBoughtOwned: String
    val PerUnit: String
    val Fees: String
    val NoPrimaryCurrencyChart: String
    val TextSample: String
    val SelectBittyTaxFile: String
    val FullPath: String
    val Open: String
    val Date: String
    val Coin: String
    val Quantity: String

    val ErrUnableToOpenXlsFile: String
}

object English : Texts {
    override val AppTitle: String = "PTracker-dev"
    override val Settings: String = "Settings"
    override val FontScaling: String = "Font scaling"
    override val CryptoCompareApiKey: String = "CryptoCompare API Key"
    override val Test: String = "Test"
    override val Holdings: String = "Holdings"
    override val Balance: String = "Balance"
    override val Cost: String = "Cost"
    override val Price: String = "Price"
    override val MarketValue: String = "Market value"
    override val ROI: String = "ROI"
    override val Asset: String = "Asset"
    override val Save: String = "Save"
    override val Ledgers: String = "Ledgers"
    override val RestartNeeded: String = "Restart needed"
    override val Unit: String = "Unit"
    override val Stats: String = "Stats"
    override val TradingStats: String = "Trading Stats"
    override val PrimaryCoin: String = "Primary Fiat Currency"
    override val ExchangeWallet: String = "Exchange/Wallet"
    override val FreeIncome: String = "Staking/Gift"
    override val NoProfitableOutcome: String = "Giveaway/Lost"
    override val TotalBoughtOwned: String = "Total bought/owned"
    override val PerUnit: String = "Per Unit"
    override val Fees: String = "Fees"
    override val NoPrimaryCurrencyChart: String = "Set primary currency in settings to see this chart"
    override val TextSample: String = "TextSample"
    override val SelectBittyTaxFile: String = "Select BittyTax Excel file"
    override val FullPath: String = "Full path to xls file"
    override val Open: String = "Open"
    override val Date: String = "Date"
    override val Coin: String = "Coin"
    override val Quantity: String = "Quantity"
    override val Fee: String = "Fee"
    override val Sell: String = "Sell"
    override val Buy: String = "Buy"
    override val ErrUnableToOpenXlsFile = "Unable to open XLS(x) file"
}