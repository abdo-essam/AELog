package com.ae.log.sample

import android.app.Application
import com.ae.log.AELog
import com.ae.log.plugins.analytics.AnalyticsPlugin
import com.ae.log.plugins.log.LogPlugin
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.interceptor.AELogKtorInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Initialise AELog with all plugins
        AELog.init(
            LogPlugin(),
            NetworkPlugin(),
            AnalyticsPlugin(),
        )

        // 2. Store plugin API references for use in commonMain screens
        SampleState.networkApi = AELog.plugin<NetworkPlugin>()?.api
        SampleState.analyticsApi = AELog.plugin<AnalyticsPlugin>()?.api

        // 3. Create a real Ktor client backed by OkHttp.
        //    AELogKtorInterceptor intercepts every request/response automatically —
        //    no manual request() / response() / newId() calls needed.
        SampleState.httpClient =
            HttpClient(OkHttp) {
                install(AELogKtorInterceptor)
            }
    }
}
