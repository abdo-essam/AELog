package com.ae.log.sample

import com.ae.log.AELog
import com.ae.log.analytics.AnalyticsPlugin
import com.ae.log.ktor.KtorInterceptor
import com.ae.log.logs.LogPlugin
import com.ae.log.network.NetworkPlugin
import io.ktor.client.HttpClient

/**
 * Simplified state management for the sample app.
 */
object SampleState {
    var httpClient: HttpClient? = null
        private set

    fun initialize() {
        if (httpClient != null) return

        // 1. Initialise AELog
        AELog.init(
            LogPlugin(),
            NetworkPlugin(),
            AnalyticsPlugin(),
        )

        // 2. Initialise HTTP client with interceptor
        httpClient =
            HttpClient {
                install(KtorInterceptor)
            }
    }
}
