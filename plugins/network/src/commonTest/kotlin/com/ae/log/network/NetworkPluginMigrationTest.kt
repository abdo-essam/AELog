package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.InternalAELogApi
import com.ae.log.plugin.Plugin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class, InternalAELogApi::class)
class NetworkPluginMigrationTest {
    @BeforeTest
    fun setUp() {
        AELog.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Records a complete GET request + 200 response into [plugin]. */
    private fun recordRequest(
        plugin: NetworkPlugin,
        url: String,
    ) {
        val id = plugin.recorder.newId()
        plugin.recorder.logRequest(id = id, method = "GET", url = url)
        plugin.recorder.logResponse(id = id, statusCode = 200)
    }

    // ── onMigrateFrom ─────────────────────────────────────────────────────

    @Test
    fun `onMigrateFrom - transfers existing network entries to new plugin`() {
        val autoPlugin = NetworkPlugin(maxEntries = 200)
        AELog.install(autoPlugin)

        recordRequest(autoPlugin, "https://api.example.com/users")
        recordRequest(autoPlugin, "https://api.example.com/orders")
        assertEquals(2, autoPlugin.storage.entries.value.size)

        val customPlugin = NetworkPlugin(maxEntries = 1000)
        AELog.configure { plugin(customPlugin) }

        assertEquals(2, customPlugin.storage.entries.value.size)
    }

    @Test
    fun `onMigrateFrom - preserves entry content url and statusCode`() {
        val autoPlugin = NetworkPlugin()
        AELog.install(autoPlugin)
        recordRequest(autoPlugin, "https://api.example.com/users")

        val customPlugin = NetworkPlugin(maxEntries = 1000)
        AELog.configure { plugin(customPlugin) }

        val migrated =
            customPlugin.storage.entries.value
                .first()
        assertEquals("https://api.example.com/users", migrated.url)
        assertEquals(200, migrated.statusCode)
    }

    @Test
    fun `onMigrateFrom - respects new plugin maxEntries capacity`() {
        val autoPlugin = NetworkPlugin(maxEntries = 200)
        AELog.install(autoPlugin)

        // Record 10 entries
        repeat(10) { i -> recordRequest(autoPlugin, "https://api.example.com/$i") }

        // New plugin has capacity of 5
        val customPlugin = NetworkPlugin(maxEntries = 5)
        AELog.configure { plugin(customPlugin) }

        assertTrue(customPlugin.storage.entries.value.size <= 5)
    }

    @Test
    fun `onMigrateFrom - new plugin starts empty when old plugin had no entries`() {
        val autoPlugin = NetworkPlugin()
        AELog.install(autoPlugin)

        val customPlugin = NetworkPlugin(maxEntries = 1000)
        AELog.configure { plugin(customPlugin) }

        assertTrue(
            customPlugin.storage.entries.value
                .isEmpty(),
        )
    }

    @Test
    fun `onMigrateFrom - ignores incompatible plugin type`() {
        class ImposterPlugin : Plugin {
            override val id = NetworkPlugin.ID
            override val name = "Imposter"
        }

        AELog.install(ImposterPlugin())

        val customPlugin = NetworkPlugin()
        AELog.configure { plugin(customPlugin) }

        // No crash; storage stays empty
        assertTrue(
            customPlugin.storage.entries.value
                .isEmpty(),
        )
    }

    @Test
    fun `onMigrateFrom - recording after migration appends to migrated entries`() {
        val autoPlugin = NetworkPlugin()
        AELog.install(autoPlugin)
        recordRequest(autoPlugin, "https://api.example.com/before")

        val customPlugin = NetworkPlugin(maxEntries = 500)
        AELog.configure { plugin(customPlugin) }

        recordRequest(customPlugin, "https://api.example.com/after")

        assertEquals(2, customPlugin.storage.entries.value.size)
    }

    @Test
    fun `onMigrateFrom - pending entries are preserved`() {
        val autoPlugin = NetworkPlugin()
        AELog.install(autoPlugin)

        // Record a request without a response (pending)
        val id = autoPlugin.recorder.newId()
        autoPlugin.recorder.logRequest(id = id, method = "POST", url = "https://api.example.com/upload")

        val customPlugin = NetworkPlugin(maxEntries = 500)
        AELog.configure { plugin(customPlugin) }

        val migrated =
            customPlugin.storage.entries.value
                .first()
        assertTrue(migrated.isPending)
        assertEquals("https://api.example.com/upload", migrated.url)
    }
}
