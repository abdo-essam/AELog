package com.ae.log.crashes.capture

/**
 * Platform-specific uncaught exception handler registration.
 *
 * Each platform target provides an actual implementation that installs
 * a global handler to forward fatal crashes to [CrashRecorder].
 */
internal expect class PlatformCrashHandler(recorder: CrashRecorder) {
    fun install()
    fun uninstall()
}
