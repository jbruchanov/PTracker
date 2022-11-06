package com.scurab.ptracker.net

import com.scurab.ptracker.app.serialisation.JsonBridge
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.http
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json

fun defaultHttpClient(
    logLevel: LogLevel = LogLevel.ALL,
    proxyUrl: String? = null,
    config: (HttpClientConfig<*>.() -> Unit) = {}
) = HttpClient {
    install(Logging) { level = logLevel }
    install(WebSockets)
    install(ContentNegotiation) {
        json(JsonBridge.json)
    }
    config()
    proxyUrl?.let { proxyUrl ->
        engine {
            this.proxy = ProxyBuilder.http(proxyUrl)
        }
    }
}
