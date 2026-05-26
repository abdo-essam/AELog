package com.ae.log.sample

import com.ae.log.ktor.AELogKtorInterceptor
import io.ktor.client.HttpClient

/**
 * Simplified state management for the sample app.
 */
object SampleState {
    var httpClient: HttpClient? = null
        private set

    fun initialize() {
        if (httpClient != null) return

        httpClient =
            HttpClient {
                install(AELogKtorInterceptor)
            }
    }
}
