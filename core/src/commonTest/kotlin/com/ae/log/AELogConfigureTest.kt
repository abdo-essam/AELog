package com.ae.log

import com.ae.log.InternalAELogApi
import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class, InternalAELogApi::class)
class AELogConfigureTest {
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
        var detached = false
        var cleared = false
        var exportValue = ""

        override fun onAttach(context: PluginContext) {
            attached = true
        }

        override fun onDetach() {
            detached = true
        }

        override fun onClear() {
            cleared = true
        }

        override fun export() = exportValue
    }

    // ── isEnabled ───────────────────────────────────────────────────

    @Test
    fun `isEnabled - defaults to true`() {
        assertTrue(AELog.isEnabled)
    }

    @Test
    fun `isEnabled - can be toggled`() {
        AELog.isEnabled = false
        assertEquals(false, AELog.isEnabled)
        AELog.isEnabled = true
        assertEquals(true, AELog.isEnabled)
    }

    // ── Plugin install / getPlugin ──────────────────────────────────────────

    @Test
    fun `getPlugin - returns null when not installed`() {
        assertNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `getPlugin - returns installed plugin`() {
        val plugin = FakePlugin()
        AELog.install(plugin)
        assertNotNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `install - calls onAttach on the plugin`() {
        val plugin = FakePlugin()
        AELog.install(plugin)
        assertTrue(plugin.attached)
    }

    @Test
    fun `install - duplicate plugin id is rejected`() {
        val first = FakePlugin(id = "dupe", name = "First")
        val second = FakePlugin(id = "dupe", name = "Second")
        AELog.install(first)
        AELog.install(second)
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
        AELog.install(plugin)
        assertTrue(AELog.export().contains("log data"))
    }

    @Test
    fun `clearAll - calls onClear on each plugin`() {
        val plugin = FakePlugin()
        AELog.install(plugin)
        AELog.clearAll()
        assertTrue(plugin.cleared)
    }

    // ── resetForTesting ────────────────────────────────────────────────────

    @Test
    fun `resetForTesting - allows re-initialization`() {
        AELog.install(FakePlugin())
        assertNotNull(AELog.instance)
        AELog.resetForTesting()
        assertNull(AELog.instance)
        AELog.install(FakePlugin())
        assertNotNull(AELog.instance)
    }

    @Test
    fun `resetForTesting - calls onDetach on plugins`() {
        val plugin = FakePlugin()
        AELog.install(plugin)
        AELog.resetForTesting()
        assertTrue(plugin.detached)
    }

    @Test
    fun `resetForTesting - resets isEnabled to true`() {
        AELog.isEnabled = false
        AELog.resetForTesting()
        assertTrue(AELog.isEnabled)
    }

    // ── Multiple plugins ──────────────────────────────────

    @Test
    fun `install - multiple plugins can be registered independently`() {
        val log = FakePlugin(id = "log", name = "Log")
        val network = FakePlugin(id = "network", name = "Network")
        AELog.install(log)
        AELog.install(network)
        assertEquals(
            2,
            AELog.instance
                ?.plugins
                ?.plugins
                ?.value
                ?.size,
        )
        assertTrue(log.attached)
        assertTrue(network.attached)
    }

    // ── showNotch global property ──────────────────────────────────────────

    @Test
    fun `showNotch - defaults to true`() {
        assertTrue(AELog.showNotch)
    }

    @Test
    fun `showNotch - can be disabled`() {
        AELog.showNotch = false
        kotlin.test.assertFalse(AELog.showNotch)
    }

    @Test
    fun `resetForTesting - resets showNotch to true`() {
        AELog.showNotch = false
        AELog.resetForTesting()
        assertTrue(AELog.showNotch)
    }
}
