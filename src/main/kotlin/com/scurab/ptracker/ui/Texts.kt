package com.scurab.ptracker.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy

val LocalTexts = compositionLocalOf(structuralEqualityPolicy()) { English }

interface Texts {
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
}