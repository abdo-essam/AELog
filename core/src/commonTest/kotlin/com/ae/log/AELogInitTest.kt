package com.ae.log

import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class AELogInitTest {
    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Fake plugin ────────────────────────────────────────────────────────

    private class FakePlugin(
        override val id: String = "fake",
        override val name: String = "Fake",
    ) : Plugin {
        var attached = false
        var started = false
        var stopped = false
        var detached = false
        var cleared = false
        var exportValue = ""

        override fun onAttach(context: PluginContext) {
            attached = true
        }

        override fun onStart() {
            started = true
        }

        override fun onStop() {
            stopped = true
        }

        override fun onDetach() {
            detached = true
        }

        override fun onClear() {
            cleared = true
        }

        override fun onOpen() {}

        override fun onClose() {}

        override fun export() = exportValue
    }

    // ── init / isEnabled ───────────────────────────────────────────────────

    @Test
    fun `init - creates instance on first call`() {
        AELog.init()
        assertNotNull(AELog.instance)
    }

    @Test
    fun `init - is idempotent and second call is a no-op`() {
        AELog.init()
        val first = AELog.instance
        AELog.init()
        assertEquals(first, AELog.instance)
    }

    @Test
    fun `isEnabled - defaults to true`() {
        AELog.init()
        assertTrue(AELog.isEnabled)
    }

    @Test
    fun `isEnabled - can be toggled`() {
        AELog.init()
        AELog.isEnabled = false
        assertEquals(false, AELog.isEnabled)
        AELog.isEnabled = true
        assertEquals(true, AELog.isEnabled)
    }

    // ── Plugin install / getPlugin ──────────────────────────────────────────

    @Test
    fun `getPlugin - returns null when not installed`() {
        AELog.init()
        assertNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `getPlugin - returns installed plugin`() {
        val plugin = FakePlugin()
        AELog.init(plugin)
        assertNotNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `install - calls onAttach on the plugin`() {
        val plugin = FakePlugin()
        AELog.init(plugin)
        assertTrue(plugin.attached)
    }

    @Test
    fun `init - duplicate plugin id is rejected`() {
        val first = FakePlugin(id = "dupe", name = "First")
        val second = FakePlugin(id = "dupe", name = "Second")
        AELog.init(first, second)
        // Only the first should be registered
        assertEquals(
            1,
            AELog.instance
                ?.plugins
                ?.plugins
                ?.value
                ?.count { it.id == "dupe" },
        )
        assertTrue(first.attached)
        assertEquals(false, second.attached)
    }

    // ── export / clearAll ──────────────────────────────────────────────────

    @Test
    fun `export - returns empty string when no instance`() {
        assertEquals("", AELog.export())
    }

    @Test
    fun `export - aggregates plugin export values`() {
        val plugin = FakePlugin().also { it.exportValue = "log data" }
        AELog.init(plugin)
        assertTrue(AELog.export().contains("log data"))
    }

    @Test
    fun `clearAll - calls onClear on each plugin`() {
        val plugin = FakePlugin()
        AELog.init(plugin)
        AELog.clearAll()
        assertTrue(plugin.cleared)
    }

    // ── resetForTesting ────────────────────────────────────────────────────

    @Test
    fun `resetForTesting - allows re-initialization`() {
        AELog.init()
        assertNotNull(AELog.instance)
        AELog.resetForTesting()
        assertNull(AELog.instance)
        AELog.init()
        assertNotNull(AELog.instance)
    }

    @Test
    fun `resetForTesting - calls onStop and onDetach on plugins`() {
        val plugin = FakePlugin()
        AELog.init(plugin)
        AELog.resetForTesting()
        assertTrue(plugin.stopped)
        assertTrue(plugin.detached)
    }

    @Test
    fun `resetForTesting - resets isEnabled to true`() {
        AELog.init()
        AELog.isEnabled = false
        AELog.resetForTesting()
        assertTrue(AELog.isEnabled)
    }
}
