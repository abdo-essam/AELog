package com.ae.log.analytics

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.ae.log.AELog

/**
 * Auto-initializer for the AELog Analytics plugin on Android.
 *
 * AGP merges the accompanying `AndroidManifest.xml` into the host app,
 * so this [ContentProvider] runs before [android.app.Application.onCreate],
 * registering [AnalyticsPlugin] with AELog automatically.
 *
 * ## Zero-config usage
 * Just add the dependency — no `AELog.configure()` call required:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:ae-log-analytics:1.0.5")
 * ```
 *
 * ## Opt-out / custom config
 * Remove the auto-initializer via manifest merger and call `AELog.configure()` yourself:
 * ```xml
 * <!-- AndroidManifest.xml -->
 * <provider
 *     android:name="com.ae.log.analytics.AnalyticsPluginInitializer"
 *     android:authorities="${applicationId}.ae_log_analytics_init"
 *     tools:node="remove" />
 * ```
 * ```kotlin
 * // Application.onCreate()
 * AELog.configure(AnalyticsPlugin(maxEntries = 1_000))
 * ```
 */
internal class AnalyticsPluginInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        AELog.registerPlugin(AnalyticsPlugin())
        return true
    }

    override fun query(
        uri: Uri,
        p: Array<String>?,
        s: String?,
        sArgs: Array<String>?,
        sort: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, s: String?, sArgs: Array<String>?): Int = 0

    override fun update(uri: Uri, values: ContentValues?, s: String?, sArgs: Array<String>?): Int = 0
}

