package com.ae.log.logs

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.InternalAELogApi
import com.ae.log.logs.model.LogSeverity
import com.ae.log.plugin.Plugin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class, InternalAELogApi::class)
class LogPluginMigrationTest {
    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Writes [count] DEBUG entries into [plugin]. */
    private fun populate(
        plugin: LogPlugin,
        count: Int,
    ) {
        repeat(count) { i ->
            plugin.recorder.log(LogSeverity.DEBUG, "Tag", "Message $i")
        }
    }

    // ── onMigrateFrom ─────────────────────────────────────────────────────

    @Test
    fun `onMigrateFrom - transfers existing log entries to new plugin`() {
        val autoPlugin = LogPlugin(maxEntries = 500)
        AELog.install(autoPlugin)

        // Write 3 entries into the auto-registered plugin
        populate(autoPlugin, 3)
        assertEquals(3, autoPlugin.logStorage.entries.value.size)

        // Consumer reconfigures with a custom instance
        val customPlugin = LogPlugin(maxEntries = 2000)
        AELog.configure { plugin(customPlugin) }

        // All 3 entries must be present in the new plugin's storage
        assertEquals(3, customPlugin.logStorage.entries.value.size)
    }

    @Test
    fun `onMigrateFrom - preserves entry content severity tag and message`() {
        val autoPlugin = LogPlugin()
        AELog.install(autoPlugin)
        autoPlugin.recorder.log(LogSeverity.WARN, "AuthTag", "Token expired")

        val customPlugin = LogPlugin(maxEntries = 2000)
        AELog.configure { plugin(customPlugin) }

        val migrated =
            customPlugin.logStorage.entries.value
                .first()
        assertEquals(LogSeverity.WARN, migrated.severity)
        assertEquals("AuthTag", migrated.tag)
        assertEquals("Token expired", migrated.message)
    }

    @Test
    fun `onMigrateFrom - respects new plugin's maxEntries capacity`() {
        val autoPlugin = LogPlugin(maxEntries = 500)
        AELog.install(autoPlugin)

        // Write 10 entries into the auto-registered plugin
        populate(autoPlugin, 10)

        // New plugin has a much tighter capacity of 5
        val customPlugin = LogPlugin(maxEntries = 5)
        AELog.configure { plugin(customPlugin) }

        // The ring buffer must not exceed the new capacity
        assertTrue(customPlugin.logStorage.entries.value.size <= 5)
    }

    @Test
    fun `onMigrateFrom - new plugin starts empty when old plugin had no entries`() {
        val autoPlugin = LogPlugin()
        AELog.install(autoPlugin)
        // No entries written

        val customPlugin = LogPlugin(maxEntries = 2000)
        AELog.configure { plugin(customPlugin) }

        assertTrue(
            customPlugin.logStorage.entries.value
                .isEmpty(),
        )
    }

    @Test
    fun `onMigrateFrom - ignores incompatible plugin type`() {
        // Register a completely different plugin type with the same id somehow
        // In practice we use a fake that shares the id but is NOT a LogPlugin
        class ImposterPlugin : Plugin {
            override val id = LogPlugin.ID
            override val name = "Imposter"
        }

        val imposter = ImposterPlugin()
        AELog.install(imposter)

        val customPlugin = LogPlugin(maxEntries = 500)
        AELog.configure { plugin(customPlugin) }

        // No crash and storage starts empty (migration silently skipped for wrong type)
        assertTrue(
            customPlugin.logStorage.entries.value
                .isEmpty(),
        )
    }

    @Test
    fun `onMigrateFrom - configure then log appends to migrated entries`() {
        val autoPlugin = LogPlugin()
        AELog.install(autoPlugin)
        populate(autoPlugin, 2)

        val customPlugin = LogPlugin(maxEntries = 500)
        AELog.configure { plugin(customPlugin) }

        // Write 1 more entry after migration
        customPlugin.recorder.log(LogSeverity.INFO, "NewTag", "Post-migration log")

        assertEquals(3, customPlugin.logStorage.entries.value.size)
    }
}
