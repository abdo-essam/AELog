package com.ae.logs.sample

import android.app.Application
import com.ae.logs.AELogs
import com.ae.logs.plugins.analytics.AnalyticsPlugin
import com.ae.logs.plugins.logs.LogsPlugin
import com.ae.logs.plugins.network.NetworkPlugin
import com.ae.logs.plugins.network.interceptor.AELogsKtorPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Initialise AELogs with all plugins
        AELogs.init(
            LogsPlugin(),
            NetworkPlugin(),
            AnalyticsPlugin(),
        )

        // 2. Store plugin API references for use in commonMain screens
        SampleState.networkApi = AELogs.plugin<NetworkPlugin>()?.api
        SampleState.analyticsApi = AELogs.plugin<AnalyticsPlugin>()?.api

        // 3. Create a real Ktor client backed by OkHttp.
        //    AELogsKtorPlugin intercepts every request/response automatically —
        //    no manual request() / response() / newId() calls needed.
        SampleState.httpClient =
            HttpClient(OkHttp) {
                install(AELogsKtorPlugin)
            }
    }
}
