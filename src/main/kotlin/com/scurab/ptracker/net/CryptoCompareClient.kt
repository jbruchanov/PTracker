package com.scurab.ptracker.net

import com.scurab.ptracker.model.CryptoCompareHistoryData
import com.scurab.ptracker.model.CryptoCompareResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class CryptoCompareClient(private val httpClient: HttpClient = defaultHttpClient()) {

    suspend fun getHistoryData(cryptoSymbol: String, fiatSymbol: String, limit: Int = 1000, toTs: Long = -1): CryptoCompareResult<CryptoCompareHistoryData> {
        return httpClient.get(historyUrl(cryptoSymbol, fiatSymbol, limit, toTs))
    }

    companion object {
        private const val mainUrl = "https://min-api.cryptocompare.com/data/v2/"
        private fun historyUrl(fsym: String, tsym: String, limit: Int, toTs: Long) = "${mainUrl}histoday?fsym=$fsym&tsym=$tsym&limit=$limit&toTs=$toTs"
    }
}