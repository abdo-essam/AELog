package com.ae.log.crashes

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.ae.log.AELog

/**
 * Auto-initializer for the AELog Crashes plugin on Android.
 *
 * AGP merges the accompanying `AndroidManifest.xml` into the host app,
 * so this [ContentProvider] runs before [android.app.Application.onCreate],
 * capturing the application [android.content.Context] automatically and
 * registering [CrashPlugin] with AELog.
 *
 * ## Zero-config usage
 * Just add the dependency — no `AELog.configure()` call required:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:ae-log-crashes:1.0.5")
 * ```
 *
 * ## Opt-out / custom config
 * Remove the auto-initializer via manifest merger and call `AELog.configure()` yourself:
 * ```xml
 * <!-- AndroidManifest.xml -->
 * <provider
 *     android:name="com.ae.log.crashes.CrashAppContextProvider"
 *     android:authorities="${applicationId}.ae_log_crash_init"
 *     tools:node="remove" />
 * ```
 * ```kotlin
 * // Application.onCreate()
 * AELog.configure(CrashPlugin(this))
 * ```
 */
internal class CrashAppContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        // 1. Capture the Application context first so defaultCrashStorageDir()
        //    resolves correctly when CrashPlugin() is constructed below.
        CrashAppContextHolder.init(ctx)
        // 2. Auto-register the plugin — same as LogPlugin/NetworkPlugin/AnalyticsPlugin.
        AELog.registerPlugin(CrashPlugin())
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

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        s: String?,
        sArgs: Array<String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        s: String?,
        sArgs: Array<String>?,
    ): Int = 0
}

