@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ae.log.crashes

import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual fun defaultCrashStorageDir(): String {
    val paths =
        NSFileManager.defaultManager.URLsForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask,
        )
    val base = (paths.firstOrNull() as? platform.Foundation.NSURL)?.path ?: ""
    return "$base/ae_crashes"
}
