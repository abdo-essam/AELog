package com.ae.log.crashes

import android.content.Context

/**
 * Android-idiomatic factory for [CrashPlugin].
 *
 * Uses [Context.filesDir] so crash events are stored in the app's private,
 * non-clearable files directory — the correct location for persistent SDK data.
 *
 * ```kotlin
 * // In Application.onCreate:
 * AELog.configure { plugin(CrashPlugin(this)) }
 * ```
 *
 * @param context Application or Activity context.
 * @param storageDir Override the storage path. Defaults to `<filesDir>/ae_crashes`.
 */
public fun CrashPlugin(
    context: Context,
    storageDir: String = context.filesDir.resolve("ae_crashes").absolutePath,
): CrashPlugin = CrashPlugin(storageDir = storageDir)
