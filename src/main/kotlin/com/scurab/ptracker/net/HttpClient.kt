package com.scurab.ptracker.net

import com.scurab.ptracker.serialisation.JsonBridge
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.http
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets

fun defaultHttpClient(
    logLevel: LogLevel = LogLevel.ALL,
    proxyUrl: String? = null,
    config: (HttpClientConfig<*>.() -> Unit) = {}
) = HttpClient {
    install(Logging) { level = logLevel }
    install(WebSockets)
    install(JsonFeature) {
        serializer = KotlinxSerializer(JsonBridge.json)
    }
    config()
    proxyUrl?.let { proxyUrl ->
        engine {
            this.proxy = ProxyBuilder.http(proxyUrl)
        }
    }
}