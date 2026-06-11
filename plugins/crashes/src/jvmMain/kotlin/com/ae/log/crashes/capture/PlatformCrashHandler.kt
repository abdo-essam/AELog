package com.ae.log.crashes.capture

/**
 * JVM actual: installs a [Thread.UncaughtExceptionHandler] on the default thread.
 *
 * Mirrors the Android implementation — runCatching guards the record call so that
 * any I/O or serialization failure does not prevent the previous handler from running.
 */
internal actual class PlatformCrashHandler actual constructor(
    private val recorder: CrashRecorder,
) {
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    actual fun install() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                recorder.record(
                    throwable = throwable,
                    threadName = thread.name,
                    isFatal = true,
                )
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    actual fun uninstall() {
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
        previousHandler = null
    }
}
