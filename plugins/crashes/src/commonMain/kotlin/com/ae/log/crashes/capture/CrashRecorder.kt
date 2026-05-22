package com.ae.log.crashes.capture

import com.ae.log.crashes.model.CrashEvent
import com.ae.log.crashes.storage.CrashStorage
import com.ae.log.utils.IdGenerator
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Records [CrashEvent] instances into [CrashStorage].
 *
 * Responsible solely for constructing a [CrashEvent] from a raw [Throwable]
 * and delegating to storage. No platform-specific handler registration here.
 */
@OptIn(ExperimentalTime::class)
internal class CrashRecorder(
    private val storage: CrashStorage,
) {
    fun record(
        throwable: Throwable,
        threadName: String,
        isFatal: Boolean,
    ) {
        val event =
            CrashEvent(
                id = IdGenerator.next(),
                timestamp = Clock.System.now().toEpochMilliseconds(),
                exceptionType = throwable::class.simpleName ?: "UnknownException",
                message = throwable.message ?: "",
                stackTrace = throwable.stackTraceToString(),
                threadName = threadName,
                isFatal = isFatal,
            )
        storage.record(event)
    }
}
