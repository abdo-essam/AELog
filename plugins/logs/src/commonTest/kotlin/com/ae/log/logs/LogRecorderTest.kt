package com.ae.log.logs

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.logs.model.LogSeverity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class LogRecorderTest {

    private lateinit var storage: LogStorage
    private lateinit var recorder: LogRecorder

    @BeforeTest
    fun setUp() {
        AELog.init(LogPlugin())
        storage = LogStorage(capacity = 100)
        recorder = LogRecorder(
            storage = storage,
            platformLogSink = PlatformLogSink.None,
        )
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Basic recording ────────────────────────────────────────────────────

    @Test
    fun `log - stores entry in storage`() {
        recorder.log(LogSeverity.DEBUG, "MyTag", "Hello world")
        assertEquals(1, storage.dataFlow.value.size)
        val entry = storage.dataFlow.value.first()
        assertEquals(LogSeverity.DEBUG, entry.severity)
        assertEquals("MyTag", entry.tag)
        assertEquals("Hello world", entry.message)
    }

    @Test
    fun `log - assigns non-blank id`() {
        recorder.log(LogSeverity.INFO, "Tag", "msg")
        assertTrue(storage.dataFlow.value.first().id.isNotBlank())
    }

    @Test
    fun `log - assigns positive timestamp`() {
        recorder.log(LogSeverity.INFO, "Tag", "msg")
        assertTrue(storage.dataFlow.value.first().timestamp > 0)
    }

    // ── Throwable handling ─────────────────────────────────────────────────

    @Test
    fun `log - appends stack trace when throwable is provided`() {
        val ex = RuntimeException("boom")
        recorder.log(LogSeverity.ERROR, "Tag", "Crashed", ex)
        val message = storage.dataFlow.value.first().message
        assertTrue(message.contains("boom"))
        assertTrue(message.contains("RuntimeException"))
    }

    @Test
    fun `log - does not append newline when throwable is null`() {
        recorder.log(LogSeverity.INFO, "Tag", "Clean message", null)
        val message = storage.dataFlow.value.first().message
        assertEquals("Clean message", message)
    }

    // ── Severity filtering ─────────────────────────────────────────────────

    @Test
    fun `log - respects minSeverity filter`() {
        val warnRecorder = LogRecorder(
            storage = storage,
            minSeverity = LogSeverity.WARN,
            platformLogSink = PlatformLogSink.None,
        )
        warnRecorder.log(LogSeverity.DEBUG, "Tag", "filtered out")
        warnRecorder.log(LogSeverity.INFO, "Tag", "also filtered")
        warnRecorder.log(LogSeverity.WARN, "Tag", "this gets through")
        warnRecorder.log(LogSeverity.ERROR, "Tag", "and this too")
        assertEquals(2, storage.dataFlow.value.size)
    }

    // ── isEnabled gate ─────────────────────────────────────────────────────

    @Test
    fun `log - is no-op when AELog isEnabled is false`() {
        AELog.isEnabled = false
        recorder.log(LogSeverity.ERROR, "Tag", "Should not be stored")
        assertTrue(storage.dataFlow.value.isEmpty())
    }

    // ── Shorthand methods ──────────────────────────────────────────────────

    @Test
    fun `v - logs VERBOSE severity`() {
        recorder.v("Tag", "verbose")
        assertEquals(LogSeverity.VERBOSE, storage.dataFlow.value.first().severity)
    }

    @Test
    fun `d - logs DEBUG severity`() {
        recorder.d("Tag", "debug")
        assertEquals(LogSeverity.DEBUG, storage.dataFlow.value.first().severity)
    }

    @Test
    fun `i - logs INFO severity`() {
        recorder.i("Tag", "info")
        assertEquals(LogSeverity.INFO, storage.dataFlow.value.first().severity)
    }

    @Test
    fun `w - logs WARN severity`() {
        recorder.w("Tag", "warn")
        assertEquals(LogSeverity.WARN, storage.dataFlow.value.first().severity)
    }

    @Test
    fun `e - logs ERROR severity`() {
        recorder.e("Tag", "error")
        assertEquals(LogSeverity.ERROR, storage.dataFlow.value.first().severity)
    }

    @Test
    fun `wtf - logs ASSERT severity`() {
        recorder.wtf("Tag", "assert")
        assertEquals(LogSeverity.ASSERT, storage.dataFlow.value.first().severity)
    }
}
