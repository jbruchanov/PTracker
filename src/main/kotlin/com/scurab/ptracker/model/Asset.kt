package com.scurab.ptracker.model

data class Asset(val crypto: String, val fiat: String) {
    fun has(value: String) = crypto == value || fiat == value
    fun has(value1: String, value2: String) = (value1 == crypto && value2 == fiat) || (value2 == crypto && value1 == fiat)
    val text = "$crypto-$fiat"
    override fun toString(): String = text

    val isEmpty = this == Empty

    companion object {
        val Empty = Asset("", "")
    }
}