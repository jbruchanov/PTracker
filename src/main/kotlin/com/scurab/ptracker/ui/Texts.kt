package com.scurab.ptracker.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy

val LocalTexts = compositionLocalOf(structuralEqualityPolicy()) { English }

interface Texts {
    val Settings: String
    val FontScaling: String
    val CryptoCompareApiKey: String
    val Test: String
    val AppTitle: String
}

object English : Texts {
    override val Settings: String = "Settings"
    override val FontScaling: String = "Font scaling"
    override val CryptoCompareApiKey: String = "CryptoCompare API Key"
    override val Test: String = "Test"
    override val AppTitle: String = "PTracker-dev"
}