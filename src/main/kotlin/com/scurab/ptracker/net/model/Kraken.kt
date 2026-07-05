@file:Suppress("ktlint:standard:filename")
package com.scurab.ptracker.net.model

import com.scurab.ptracker.app.model.Asset
import com.scurab.ptracker.app.model.ExchangeWallet
import com.scurab.ptracker.app.model.WsExchangeResponse
import java.math.BigDecimal

sealed class KrakenWsResponse : WsExchangeResponse {

    override val client: String = "Kraken"

    object HeartBeat : KrakenWsResponse(), WsExchangeResponse.HeartBeat

    data class SubscriptionDone(
        override val exchangeWallet: ExchangeWallet,
        override val asset: Asset
    ) : KrakenWsResponse(), WsExchangeResponse.Subscription

    data class SubscriptionError(
        override val message: String
    ) : KrakenWsResponse(), WsExchangeResponse.SubscriptionError

    data class MarketTicker(
        override val asset: Asset,
        override val price: BigDecimal
    ) : KrakenWsResponse(), WsExchangeResponse.MarketPrice
}
