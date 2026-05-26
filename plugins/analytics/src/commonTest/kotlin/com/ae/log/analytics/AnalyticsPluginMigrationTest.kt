package com.ae.log.analytics

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.analytics.model.DefaultAdapterSource
import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class AnalyticsPluginMigrationTest {
    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun track(
        plugin: AnalyticsPlugin,
        name: String,
    ) {
        plugin.tracker.track(name)
    }

    // ── onMigrateFrom ─────────────────────────────────────────────────────

    @Test
    fun `onMigrateFrom - transfers existing events to new plugin`() {
        val autoPlugin = AnalyticsPlugin(maxEntries = 500)
        AELog.registerPlugin(autoPlugin)

        track(autoPlugin, "app_open")
        track(autoPlugin, "button_tap")
        track(autoPlugin, "purchase")
        assertEquals(3, autoPlugin.storage.events.value.size)

        val customPlugin = AnalyticsPlugin(maxEntries = 2000)
        AELog.configure(customPlugin)

        assertEquals(3, customPlugin.storage.events.value.size)
    }

    @Test
    fun `onMigrateFrom - preserves event name and properties`() {
        val autoPlugin = AnalyticsPlugin()
        AELog.registerPlugin(autoPlugin)
        autoPlugin.tracker.track("purchase", mapOf("item" to "premium", "price" to "9.99"))

        val customPlugin = AnalyticsPlugin(maxEntries = 2000)
        AELog.configure(customPlugin)

        val migrated =
            customPlugin.storage.events.value
                .first()
        assertEquals("purchase", migrated.name)
        assertEquals("premium", migrated.properties["item"])
        assertEquals("9.99", migrated.properties["price"])
    }

    @Test
    fun `onMigrateFrom - preserves source adapter`() {
        val autoPlugin = AnalyticsPlugin()
        AELog.registerPlugin(autoPlugin)
        autoPlugin.tracker.track("login", source = DefaultAdapterSource.FIREBASE)

        val customPlugin = AnalyticsPlugin(maxEntries = 2000)
        AELog.configure(customPlugin)

        val migrated =
            customPlugin.storage.events.value
                .first()
        assertEquals("Firebase", migrated.source?.sourceName)
    }

    @Test
    fun `onMigrateFrom - respects new plugin maxEntries capacity`() {
        val autoPlugin = AnalyticsPlugin(maxEntries = 500)
        AELog.registerPlugin(autoPlugin)

        repeat(10) { i -> track(autoPlugin, "event_$i") }

        val customPlugin = AnalyticsPlugin(maxEntries = 5)
        AELog.configure(customPlugin)

        assertTrue(customPlugin.storage.events.value.size <= 5)
    }

    @Test
    fun `onMigrateFrom - new plugin starts empty when old plugin had no events`() {
        val autoPlugin = AnalyticsPlugin()
        AELog.registerPlugin(autoPlugin)

        val customPlugin = AnalyticsPlugin(maxEntries = 2000)
        AELog.configure(customPlugin)

        assertTrue(
            customPlugin.storage.events.value
                .isEmpty(),
        )
    }

    @Test
    fun `onMigrateFrom - ignores incompatible plugin type`() {
        class ImposterPlugin : Plugin {
            override val id = AnalyticsPlugin.ID
            override val name = "Imposter"

            override fun onAttach(context: PluginContext) {}

            override fun onStart() {}

            override fun onStop() {}

            override fun onDetach() {}

            override fun onClear() {}

            override fun onOpen() {}

            override fun onClose() {}

            override fun export() = ""
        }

        AELog.registerPlugin(ImposterPlugin())

        val customPlugin = AnalyticsPlugin()
        AELog.configure(customPlugin)

        assertTrue(
            customPlugin.storage.events.value
                .isEmpty(),
        )
    }

    @Test
    fun `onMigrateFrom - events tracked after migration append to migrated events`() {
        val autoPlugin = AnalyticsPlugin()
        AELog.registerPlugin(autoPlugin)
        track(autoPlugin, "pre_migration_event")

        val customPlugin = AnalyticsPlugin(maxEntries = 500)
        AELog.configure(customPlugin)

        track(customPlugin, "post_migration_event")

        assertEquals(2, customPlugin.storage.events.value.size)
        assertEquals(
            "pre_migration_event",
            customPlugin.storage.events.value[0]
                .name,
        )
        assertEquals(
            "post_migration_event",
            customPlugin.storage.events.value[1]
                .name,
        )
    }

    @Test
    fun `onMigrateFrom - screen events are preserved`() {
        val autoPlugin = AnalyticsPlugin()
        AELog.registerPlugin(autoPlugin)
        autoPlugin.tracker.screen("HomeScreen", mapOf("source" to "deeplink"))

        val customPlugin = AnalyticsPlugin(maxEntries = 500)
        AELog.configure(customPlugin)

        val migrated =
            customPlugin.storage.events.value
                .first()
        assertEquals("screen_view", migrated.name)
        assertEquals("HomeScreen", migrated.properties["screen"])
        assertEquals("deeplink", migrated.properties["source"])
    }
}
