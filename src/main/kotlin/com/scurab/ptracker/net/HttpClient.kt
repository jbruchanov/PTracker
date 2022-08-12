package com.scurab.ptracker.net

import com.scurab.ptracker.app.serialisation.JsonBridge
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*

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