package com.ae.log.crashes.capture

/**
 * Android actual: installs a [Thread.UncaughtExceptionHandler] on the default thread.
 *
 * - The previous handler is always chained so the system crash dialog still appears.
 * - [recorder.record] is wrapped in runCatching so that any serialization or I/O
 *   error inside the handler does NOT swallow the crash silently — the previous
 *   handler is still called regardless.
 */
internal actual class PlatformCrashHandler actual constructor(
    private val recorder: CrashRecorder,
) {
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    actual fun install() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Guarded — any failure inside record() must not prevent the chain call
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
