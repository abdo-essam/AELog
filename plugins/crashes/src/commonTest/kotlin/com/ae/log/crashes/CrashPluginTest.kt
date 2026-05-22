package com.ae.log.crashes

import com.ae.log.crashes.model.CrashEvent
import com.ae.log.crashes.model.CrashFilter
import com.ae.log.crashes.ui.CrashUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CrashFilterTest {
    private fun fatalEvent() = CrashEvent(
        id = "1",
        timestamp = Clock.System.now().toEpochMilliseconds(),
        exceptionType = "NullPointerException",
        message = "null ref",
        stackTrace = "at Foo.bar(Foo.kt:1)",
        threadName = "main",
        isFatal = true,
    )

    private fun nonFatalEvent() = fatalEvent().copy(id = "2", isFatal = false)

    @Test
    fun allFilterMatchesBoth() {
        assertTrue(CrashFilter.ALL.matches(fatalEvent()))
        assertTrue(CrashFilter.ALL.matches(nonFatalEvent()))
    }

    @Test
    fun fatalFilterOnlyMatchesFatal() {
        assertTrue(CrashFilter.FATAL.matches(fatalEvent()))
        assertEquals(false, CrashFilter.FATAL.matches(nonFatalEvent()))
    }

    @Test
    fun nonFatalFilterOnlyMatchesNonFatal() {
        assertTrue(CrashFilter.NON_FATAL.matches(nonFatalEvent()))
        assertEquals(false, CrashFilter.NON_FATAL.matches(fatalEvent()))
    }
}

class CrashUtilsTest {
    private val event = CrashEvent(
        id = "x",
        timestamp = 0L,
        exceptionType = "IllegalStateException",
        message = "bad state",
        stackTrace = "at Bar.baz(Bar.kt:42)",
        threadName = "worker-1",
        isFatal = false,
    )

    @Test
    fun formatEventForCopyContainsKeyFields() {
        val output = CrashUtils.formatEventForCopy(event)
        assertTrue(output.contains("NON-FATAL"))
        assertTrue(output.contains("IllegalStateException"))
        assertTrue(output.contains("worker-1"))
        assertTrue(output.contains("bad state"))
        assertTrue(output.contains("Bar.baz"))
    }

    @Test
    fun formatAllEventsJoinsWithSeparator() {
        val events = listOf(event, event.copy(id = "y"))
        val output = CrashUtils.formatAllEventsForCopy(events)
        assertTrue(output.contains("---"))
    }
}
