package com.ae.logs.sample

import com.ae.logs.plugins.analytics.AnalyticsApi
import com.ae.logs.plugins.network.NetworkApi
import io.ktor.client.HttpClient

/**
 * Singleton that holds plugin API references after [SampleApp.onCreate].
 *
 * Lives in commonMain so [App] (also commonMain) can access it without
 * using reified inline functions (which cause JVM target mismatches in KMP).
 *
 * APIs are `null` before init — all callers guard with `?.`.
 */
object SampleState {
    var networkApi: NetworkApi? = null
    var analyticsApi: AnalyticsApi? = null

    /**
     * Real [HttpClient] created in [SampleApp] with [AELogsKtorPlugin] installed.
     * Every call made through this client is automatically captured by [NetworkPlugin].
     */
    var httpClient: HttpClient? = null
}
