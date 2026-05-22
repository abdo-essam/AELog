package com.ae.log.crashes

import android.content.Context

/**
 * Holds a reference to the [Application][android.app.Application] context.
 *
 * Populated by [CrashAppContextProvider] before [Application.onCreate] runs.
 * Uses @Volatile + null-check instead of atomicfu to avoid the compiler plugin
 * requirement — ContentProvider.onCreate() is always called on the main thread,
 * so a single volatile write is sufficient.
 */
internal object CrashAppContextHolder {
    @Volatile private var _context: Context? = null

    internal val context: Context?
        get() = _context

    internal fun init(context: Context) {
        if (_context == null) {
            _context = context.applicationContext
        }
    }
}
