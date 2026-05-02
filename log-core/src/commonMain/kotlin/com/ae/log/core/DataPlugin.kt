package com.ae.log.core

/**
 * A headless plugin that collects or processes data without providing a UI panel.
 *
 * Use this for background tasks like crash recording, performance sampling,
 * or analytics collection that don't need a visible tab in AELog.
 *
 * Data plugins are queryable programmatically:
 * ```kotlin
 * val crash = inspector.getPlugin<MyCrashPlugin>()
 * crash?.recentCrashes
 * ```
 */
public interface DataPlugin : AELogPlugin
