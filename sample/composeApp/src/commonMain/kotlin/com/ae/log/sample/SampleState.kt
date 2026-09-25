package com.ae.log.sample

import com.ae.log.ktor.AELogKtorInterceptor
import io.ktor.client.HttpClient

/**
 * Simplified state management for the sample app.
 *
 * Plugins (Logs, Crashes, Network, Analytics, Database) are auto-initialized
 * by AELog's platform initializers on Android, iOS, and Wasm.
 */
object SampleState {
    val httpClient: HttpClient by lazy {
        HttpClient {
            install(AELogKtorInterceptor)
        }
    }

    fun initialize() {
        runCatching { ensureSampleDatabaseExists() }
        httpClient
    }
}
