package com.scurab.ptracker.model

import java.io.File

data class Asset(val crypto: String, val fiat: String) {
    fun has(value: String) = crypto == value || fiat == value
    fun has(value1: String, value2: String) = (value1 == crypto && value2 == fiat) || (value2 == crypto && value1 == fiat)
    val label = buildString {
        append(crypto)
        if (isNotEmpty() && fiat.isNotEmpty()) {
            append("-")
        }
        append(fiat)
    }

    override fun toString(): String = label

    val isEmpty = this == Empty

    fun iconCrypto() = File(Locations.Icons, crypto.lowercase() + ".png")

    companion object {
        val Empty = Asset("", "")
    }
}