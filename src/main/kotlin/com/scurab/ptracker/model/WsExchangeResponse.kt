package com.scurab.ptracker.model

interface WsExchangeResponse {

    val client:String

    interface HeartBeat : WsExchangeResponse
    interface SubscriptionComplete : WsExchangeResponse
    interface MarketPrice : com.scurab.ptracker.model.MarketPrice

    interface Error : WsExchangeResponse {
        val message: String
    }

    interface SubscriptionError : WsExchangeResponse {
        val message: String
    }

    interface Subscription : WsExchangeResponse {
        val exchangeWallet: ExchangeWallet
        val asset: Asset
    }
}

