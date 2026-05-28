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
        var started = false
        var stopped = false
        var detached = false
        var cleared = false
        var exportValue = ""
        var migratedFrom: Plugin? = null

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

        override fun onMigrateFrom(oldPlugin: Plugin) {
            migratedFrom = oldPlugin
        }

        override fun onOpen() {}

        override fun onClose() {}

        override fun export() = exportValue
    }

    // ── configure / isEnabled ───────────────────────────────────────────────────

    @Test
    fun `configure - creates instance on first call`() {
        AELog.configure { }
        assertNotNull(AELog.instance)
    }

    @Test
    fun `configure - is idempotent and second call is a no-op`() {
        AELog.configure { }
        val first = AELog.instance
        AELog.configure { }
        assertEquals(first, AELog.instance)
    }

    @Test
    fun `isEnabled - defaults to true`() {
        AELog.configure { }
        assertTrue(AELog.isEnabled)
    }

    @Test
    fun `isEnabled - can be toggled`() {
        AELog.configure { }
        AELog.isEnabled = false
        assertEquals(false, AELog.isEnabled)
        AELog.isEnabled = true
        assertEquals(true, AELog.isEnabled)
    }

    // ── Plugin install / getPlugin ──────────────────────────────────────────

    @Test
    fun `getPlugin - returns null when not installed`() {
        AELog.configure { }
        assertNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `getPlugin - returns installed plugin`() {
        val plugin = FakePlugin()
        AELog.configure { plugin(plugin) }
        assertNotNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `install - calls onAttach on the plugin`() {
        val plugin = FakePlugin()
        AELog.configure { plugin(plugin) }
        assertTrue(plugin.attached)
    }

    @Test
    fun `configure - duplicate plugin id is rejected`() {
        val first = FakePlugin(id = "dupe", name = "First")
        val second = FakePlugin(id = "dupe", name = "Second")
        AELog.configure {
            plugin(first)
            plugin(second)
        }
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
        AELog.configure { plugin(plugin) }
        assertTrue(AELog.export().contains("log data"))
    }

    @Test
    fun `clearAll - calls onClear on each plugin`() {
        val plugin = FakePlugin()
        AELog.configure { plugin(plugin) }
        AELog.clearAll()
        assertTrue(plugin.cleared)
    }

    // ── resetForTesting ────────────────────────────────────────────────────

    @Test
    fun `resetForTesting - allows re-initialization`() {
        AELog.configure { }
        assertNotNull(AELog.instance)
        AELog.resetForTesting()
        assertNull(AELog.instance)
        AELog.configure { }
        assertNotNull(AELog.instance)
    }

    @Test
    fun `resetForTesting - calls onStop and onDetach on plugins`() {
        val plugin = FakePlugin()
        AELog.configure { plugin(plugin) }
        AELog.resetForTesting()
        assertTrue(plugin.stopped)
        assertTrue(plugin.detached)
    }

    @Test
    fun `resetForTesting - resets isEnabled to true`() {
        AELog.configure { }
        AELog.isEnabled = false
        AELog.resetForTesting()
        assertTrue(AELog.isEnabled)
    }

    // ── Zero-config / registerPlugin (simulates ContentProvider auto-init) ──

    @Test
    fun `registerPlugin - creates instance on first call`() {
        val plugin = FakePlugin()
        AELog.install(plugin)
        assertNotNull(AELog.instance)
    }

    @Test
    fun `registerPlugin - installs plugin and calls onAttach`() {
        val plugin = FakePlugin()
        AELog.install(plugin)
        assertTrue(plugin.attached)
        assertNotNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `registerPlugin - multiple plugins can be registered independently`() {
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

    @Test
    fun `registerPlugin - duplicate id is silently ignored`() {
        val first = FakePlugin(id = "log", name = "First")
        val second = FakePlugin(id = "log", name = "Second")
        AELog.install(first)
        AELog.install(second)
        assertEquals(
            1,
            AELog.instance
                ?.plugins
                ?.plugins
                ?.value
                ?.count { it.id == "log" },
        )
        assertTrue(first.attached)
        assertEquals(false, second.attached)
    }

    // ── Migration: configure() replacing an auto-registered plugin ──────────

    @Test
    fun `configure - calls onMigrateFrom when replacing an existing plugin`() {
        // Simulate auto-init (ContentProvider registers a default plugin)
        val autoPlugin = FakePlugin(id = "log", name = "AutoLog")
        AELog.install(autoPlugin)

        // Consumer calls configure() with a custom version of the same plugin
        val customPlugin = FakePlugin(id = "log", name = "CustomLog")
        AELog.configure { plugin(customPlugin) }

        // onMigrateFrom must be called with the old auto-registered instance
        assertEquals(autoPlugin, customPlugin.migratedFrom)
    }

    @Test
    fun `configure - old plugin is detached after migration`() {
        val autoPlugin = FakePlugin(id = "log", name = "AutoLog")
        AELog.install(autoPlugin)

        val customPlugin = FakePlugin(id = "log", name = "CustomLog")
        AELog.configure { plugin(customPlugin) }

        // The auto plugin must be detached and removed
        assertTrue(autoPlugin.detached)
        assertEquals(
            1,
            AELog.instance
                ?.plugins
                ?.plugins
                ?.value
                ?.count { it.id == "log" },
        )
    }

    @Test
    fun `configure - new plugin is installed and attached after migration`() {
        val autoPlugin = FakePlugin(id = "log", name = "AutoLog")
        AELog.install(autoPlugin)

        val customPlugin = FakePlugin(id = "log", name = "CustomLog")
        AELog.configure { plugin(customPlugin) }

        // The custom plugin should now be the active one
        assertTrue(customPlugin.attached)
        assertNotNull(AELog.getPlugin<FakePlugin>())
    }

    @Test
    fun `configure - does NOT call onMigrateFrom when no existing plugin with same id`() {
        // Fresh configure() with no prior auto-registered plugin for this id
        val plugin = FakePlugin(id = "analytics", name = "Analytics")
        AELog.configure { plugin(plugin) }

        assertNull(plugin.migratedFrom)
        assertTrue(plugin.attached)
    }

    @Test
    fun `configure - plugins NOT mentioned in configure are left as-is`() {
        // Auto-init registers two plugins
        val logPlugin = FakePlugin(id = "log", name = "Log")
        val networkPlugin = FakePlugin(id = "network", name = "Network")
        AELog.install(logPlugin)
        AELog.install(networkPlugin)

        // Consumer only reconfigures log — network must remain untouched
        val customLog = FakePlugin(id = "log", name = "CustomLog")
        AELog.configure { plugin(customLog) }

        // Network plugin must still be registered and not detached
        assertEquals(false, networkPlugin.detached)
        assertEquals(
            2,
            AELog.instance
                ?.plugins
                ?.plugins
                ?.value
                ?.size,
        )
    }

    @Test
    fun `configure - second configure call replaces first custom plugin with migration`() {
        val v1 = FakePlugin(id = "log", name = "LogV1")
        AELog.configure { plugin(v1) }

        val v2 = FakePlugin(id = "log", name = "LogV2")
        AELog.configure { plugin(v2) }

        // v2 should have migrated from v1
        assertEquals(v1, v2.migratedFrom)
        assertTrue(v1.detached)
        assertTrue(v2.attached)
    }

    @Test
    fun `configure - duplicate ids in same call - only first is installed`() {
        val first = FakePlugin(id = "log", name = "First")
        val second = FakePlugin(id = "log", name = "Second")
        AELog.configure {
            plugin(first)
            plugin(second)
        }

        // distinctBy keeps only first; second is dropped before install
        assertTrue(first.attached)
        assertEquals(false, second.attached)
        // first had no prior plugin to migrate from
        assertNull(first.migratedFrom)
    }
}
