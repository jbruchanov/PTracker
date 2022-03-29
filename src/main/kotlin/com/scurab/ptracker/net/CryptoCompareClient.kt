package com.scurab.ptracker.net

import com.scurab.ptracker.net.model.CryptoCoinDetail
import com.scurab.ptracker.net.model.CryptoCompareHistoryData
import com.scurab.ptracker.net.model.CryptoCompareResult
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class CryptoCompareClient(private val httpClient: HttpClient = defaultHttpClient()) {

    suspend fun getHistoryData(cryptoSymbol: String, fiatSymbol: String, limit: Int = 1000, toTs: Long = -1): CryptoCompareResult<CryptoCompareHistoryData> {
        return httpClient.get(historyUrl(cryptoSymbol, fiatSymbol, limit, toTs))
    }

    suspend fun getCoinData(cryptoSymbol: String): CryptoCompareResult<Map<String, CryptoCoinDetail>> {
        return httpClient.get(coinUrl(cryptoSymbol))
    }

    companion object {
        private const val mainUrl = "https://min-api.cryptocompare.com"
        private fun historyUrl(fsym: String, tsym: String, limit: Int, toTs: Long) = "${mainUrl}/data/v2/histoday?fsym=$fsym&tsym=$tsym&limit=$limit&toTs=$toTs"
        private fun coinUrl(fsym: String) = "${mainUrl}/data/all/coinlist?fsym=$fsym"
    }
}