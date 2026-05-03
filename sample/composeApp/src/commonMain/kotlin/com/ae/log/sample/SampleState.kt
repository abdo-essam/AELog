package com.ae.log.sample

import com.ae.log.AELog
import com.ae.log.plugins.analytics.AnalyticsPlugin
import com.ae.log.plugins.analytics.AnalyticsTracker
import com.ae.log.plugins.log.LogPlugin
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.NetworkRecorder
import com.ae.log.plugins.network.interceptor.KtorInterceptor
import io.ktor.client.HttpClient
import kotlinx.atomicfu.atomic

/**
 * Singleton that holds plugin API references.
 *
 * Lives in commonMain so [App] can access it without Target mismatches.
 */
object SampleState {
    private val _networkApi = atomic<NetworkRecorder?>(null)
    val networkApi: NetworkRecorder? get() = _networkApi.value

    private val _analyticsApi = atomic<AnalyticsTracker?>(null)
    val analyticsApi: AnalyticsTracker? get() = _analyticsApi.value

    private val _httpClient = atomic<HttpClient?>(null)
    val httpClient: HttpClient? get() = _httpClient.value

    private val isInitialized = atomic(false)

    fun initialize() {
        if (isInitialized.compareAndSet(expect = false, update = true)) {
            AELog.init(
                LogPlugin(),
                NetworkPlugin(),
                AnalyticsPlugin(),
            )

            _networkApi.value = AELog.plugin<NetworkPlugin>()?.recorder
            _analyticsApi.value = AELog.plugin<AnalyticsPlugin>()?.tracker
            _httpClient.value =
                HttpClient {
                    install(KtorInterceptor)
                }
        }
    }
}
