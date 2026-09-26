package com.ae.log.database

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.ae.log.AELog

/**
 * Auto-initializer for the AELog Database plugin on Android.
 *
 * Captures the application context before [android.app.Application.onCreate] runs,
 * and automatically registers [DatabasePlugin] with AELog.
 */
internal class DatabasePluginInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        val ctx = context ?: return false
        DatabaseAppContextHolder.init(ctx)
        AELog.install(DatabasePlugin())
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0
}
