package com.ae.log.crashes

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Auto-initializer for the AELog Crashes plugin on Android.
 *
 * AGP merges the accompanying `AndroidManifest.xml` into the host app,
 * so this [ContentProvider] runs before [android.app.Application.onCreate],
 * capturing the application [android.content.Context] automatically.
 *
 * Consumers need zero setup — `CrashPlugin()` resolves the correct path
 * without any explicit Context parameter.
 */
internal class CrashAppContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        CrashAppContextHolder.init(context ?: return false)
        return true
    }

    override fun query(uri: Uri, p: Array<String>?, s: String?, sArgs: Array<String>?, sort: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, sArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, s: String?, sArgs: Array<String>?): Int = 0
}
