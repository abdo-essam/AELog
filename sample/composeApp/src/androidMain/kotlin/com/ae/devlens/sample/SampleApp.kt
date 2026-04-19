package com.ae.devlens.sample

import android.app.Application
import com.ae.devlens.DevLensSetup
import com.ae.devlens.plugins.analytics.AnalyticsPlugin
import com.ae.devlens.plugins.logs.LogsPlugin
import com.ae.devlens.plugins.network.NetworkPlugin

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create plugins first so we can extract their APIs before installing
        val networkPlugin = NetworkPlugin()
        val analyticsPlugin = AnalyticsPlugin()

        DevLensSetup.init(
            plugins =
                listOf(
                    LogsPlugin(),
                    networkPlugin,
                    analyticsPlugin,
                ),
        )

        // Expose APIs to commonMain via SampleState — avoids reified inline calls in App.kt
        SampleState.networkApi = networkPlugin.api
        SampleState.analyticsApi = analyticsPlugin.api
    }
}
