package com.ae.log.crashes

/**
 * Returns the platform-default directory path for crash storage.
 *
 * - **Android**: `<filesDir>/ae_crashes`
 * - **iOS**: `<ApplicationSupportDirectory>/ae_crashes`
 * - **JVM**: `<user.home>/.ae_crashes`
 */
internal expect fun defaultCrashStorageDir(): String
