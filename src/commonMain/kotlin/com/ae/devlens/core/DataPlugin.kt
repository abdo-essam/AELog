package com.ae.devlens.core

/**
 * A headless plugin that collects/processes data without providing a UI panel.
 *
 * Use this for background tasks like crash recording, performance sampling,
 * or analytics collection that don't need a visible tab.
 *
 * Data plugins can be queried programmatically:
 * ```kotlin
 * val crashPlugin = inspector.getPlugin<MyCrashPlugin>()
 * crashPlugin?.recentCrashes
 * ```
 */
interface DataPlugin : DevLensPlugin
