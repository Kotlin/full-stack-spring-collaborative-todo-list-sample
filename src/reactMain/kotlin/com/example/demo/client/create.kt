package com.example.demo.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.rsocket.kotlin.core.RSocketConnector
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.ktor.client.RSocketSupport
import io.rsocket.kotlin.ktor.client.rSocket
import io.rsocket.kotlin.payload.PayloadMimeType
import kotlinx.browser.window

actual suspend fun RSocketClient.Companion.create(): Client {
    val client: HttpClient = HttpClient {
        install(WebSockets)
        install(RSocketSupport) {
            connector = RSocketConnector {
                connectionConfig {
                    payloadMimeType = PayloadMimeType(
                        data = WellKnownMimeType.ApplicationJson,
                        metadata = WellKnownMimeType.MessageRSocketCompositeMetadata
                    )
                }
            }
        }
    }

    val rSocket = client.rSocket(
        host = window.location.hostname,
        port = window.location.port.toInt(),
        path = "/rsocket"
    )

    return RSocketClient(rSocket)
}