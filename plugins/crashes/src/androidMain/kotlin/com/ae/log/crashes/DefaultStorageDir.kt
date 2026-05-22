package com.ae.log.crashes

/**
 * Resolves the app-private files directory via [CrashAppContextHolder].
 *
 * [CrashAppContextProvider] populates the context before [android.app.Application.onCreate],
 * so this returns the correct path in all normal usage scenarios.
 *
 * Falls back to the app's tmpdir if context is not yet available
 * (e.g., during instrumented tests that bypass the ContentProvider lifecycle).
 */
internal actual fun defaultCrashStorageDir(): String {
    val ctx = CrashAppContextHolder.context
    return if (ctx != null) {
        ctx.filesDir.resolve("ae_crashes").absolutePath
    } else {
        "${System.getProperty("java.io.tmpdir", "/tmp")}/ae_crashes"
    }
}
