package com.scurab.ptracker.net

import com.scurab.ptracker.app.serialisation.JsonBridge
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*

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