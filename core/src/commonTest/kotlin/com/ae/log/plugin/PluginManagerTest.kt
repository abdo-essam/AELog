package com.ae.log.plugin

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.config.LogConfig
import com.ae.log.event.EventBus
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class PluginManagerTest {
    private val config = LogConfig()
    private val eventBus = EventBus()
    private val manager = PluginManager(config, eventBus)

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Fake plugin helper ────────────────────────────────────────────────

    private class FakePlugin(
        override val id: String,
        override val name: String = id,
    ) : Plugin {
        var attachCount = 0
        var detachCount = 0
        var clearCount = 0
        var lastContext: PluginContext? = null

        override fun onAttach(context: PluginContext) {
            attachCount++
            lastContext = context
        }

        override fun onStart() {}

        override fun onStop() {}

        override fun onDetach() {
            detachCount++
        }

        override fun onClear() {
            clearCount++
        }

        override fun onOpen() {}

        override fun onClose() {}

        override fun export() = ""
    }

    // ── install ───────────────────────────────────────────────────────────

    @Test
    fun `install - adds plugin to the list`() {
        val plugin = FakePlugin("p1")
        manager.install(plugin)
        assertEquals(1, manager.plugins.value.size)
        assertEquals(
            "p1",
            manager.plugins.value
                .first()
                .id,
        )
    }

    @Test
    fun `install - calls onAttach with a non-null context`() {
        val plugin = FakePlugin("p1")
        manager.install(plugin)
        assertTrue(plugin.attachCount == 1)
        assertNotNull(plugin.lastContext)
    }

    @Test
    fun `install - duplicate id is ignored`() {
        val a = FakePlugin("dupe")
        val b = FakePlugin("dupe")
        manager.install(a)
        manager.install(b)
        assertEquals(1, manager.plugins.value.size)
        assertEquals(1, a.attachCount)
        assertEquals(0, b.attachCount)
    }

    @Test
    fun `install - multiple different plugins are all registered`() {
        manager.install(FakePlugin("a"))
        manager.install(FakePlugin("b"))
        manager.install(FakePlugin("c"))
        assertEquals(3, manager.plugins.value.size)
    }

    @Test
    fun `install - returns this for chaining`() {
        val result = manager.install(FakePlugin("p1")).install(FakePlugin("p2"))
        assertEquals(manager, result)
        assertEquals(2, manager.plugins.value.size)
    }

    // ── uninstall ─────────────────────────────────────────────────────────

    @Test
    fun `uninstall - removes plugin from the list`() {
        manager.install(FakePlugin("p1"))
        manager.uninstall("p1")
        assertTrue(manager.plugins.value.isEmpty())
    }

    @Test
    fun `uninstall - calls onDetach`() {
        val plugin = FakePlugin("p1")
        manager.install(plugin)
        manager.uninstall("p1")
        assertEquals(1, plugin.detachCount)
    }

    @Test
    fun `uninstall - unknown id is a no-op`() {
        manager.install(FakePlugin("p1"))
        manager.uninstall("unknown")
        assertEquals(1, manager.plugins.value.size)
    }

    // ── uninstallAll ──────────────────────────────────────────────────────

    @Test
    fun `uninstallAll - removes all plugins`() {
        manager.install(FakePlugin("a"))
        manager.install(FakePlugin("b"))
        manager.uninstallAll()
        assertTrue(manager.plugins.value.isEmpty())
    }

    @Test
    fun `uninstallAll - calls onDetach on each plugin`() {
        val a = FakePlugin("a")
        val b = FakePlugin("b")
        manager.install(a)
        manager.install(b)
        manager.uninstallAll()
        assertEquals(1, a.detachCount)
        assertEquals(1, b.detachCount)
    }

    // ── getPlugin ─────────────────────────────────────────────────────────

    @Test
    fun `getPlugin - returns plugin by type`() {
        manager.install(FakePlugin("p1"))
        assertNotNull(manager.getPlugin(FakePlugin::class))
    }

    @Test
    fun `getPlugin - returns null when not installed`() {
        assertNull(manager.getPlugin(FakePlugin::class))
    }

    @Test
    fun `getPluginById - returns plugin by id`() {
        manager.install(FakePlugin("p1"))
        assertNotNull(manager.getPluginById("p1"))
    }

    @Test
    fun `getPluginById - returns null for unknown id`() {
        assertNull(manager.getPluginById("unknown"))
    }
}
