package com.ae.log.database

import android.content.Context

/**
 * Holds application context captured by [DatabasePluginInitializer].
 */
internal object DatabaseAppContextHolder {
    @Volatile private var contextRef: Context? = null

    internal val context: Context?
        get() = contextRef

    internal fun init(context: Context) {
        if (contextRef == null) {
            contextRef = context.applicationContext
        }
    }
}
