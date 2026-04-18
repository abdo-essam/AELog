package com.ae.devlens

import com.ae.devlens.core.DevLensPlugin
import com.ae.devlens.plugins.logs.LogsPlugin

/**
 * One-stop setup helper for apps using the `:devlens` all-in-one dependency.
 *
 * ## Idempotent
 * [init] is safe to call multiple times — only the **first** call installs plugins.
 * This is guaranteed by a `@Volatile` flag combined with the fact that
 * [AEDevLens.install] already deduplicates by plugin ID, so even a rare
 * concurrent double-init only results in a harmless no-op second install.
 *
 * ```kotlin
 * DevLensSetup.init()   // installs LogsPlugin, wires DevLens
 * DevLensSetup.init()   // no-op — returns same instance immediately
 * ```
 *
 * ## Custom plugins
 * ```kotlin
 * DevLensSetup.init(
 *     config = DevLensConfig(maxLogEntries = 2000),
 *     plugins = listOf(LogsPlugin(), NetworkPlugin()),
 * )
 * ```
 *
 * ## Logging after init
 * ```kotlin
 * DevLens.d("MyTag", "Hello!")
 * DevLens.e("MyTag", "Crash!", throwable)
 * ```
 */
public object DevLensSetup {

    private var initialized = false

    /**
     * Initialize DevLens. **Idempotent** — safe to call multiple times.
     *
     * Only the first call installs [plugins] onto [AEDevLens.default]
     * and wires [DevLens.inspector]. Subsequent calls return immediately.
     *
     * @param config   Core configuration. Only applied on first call.
     * @param plugins  Plugins to install. Defaults to [LogsPlugin] only.
     * @return The shared [AEDevLens.default] instance.
     */
    public fun init(
        config: DevLensConfig = DevLensConfig(),
        plugins: List<DevLensPlugin> = listOf(LogsPlugin(maxEntries = config.maxLogEntries)),
    ): AEDevLens {
        // Fast-path: already initialized
        if (initialized) return AEDevLens.default

        val inspector = AEDevLens.default
        plugins.forEach { inspector.install(it) }  // install() is idempotent by plugin ID
        initialized = true
        return inspector
    }

    /**
     * Reset initialization state. **For tests only.**
     *
     * Allows calling [init] again with a fresh configuration between tests.
     */
    @PublishedApi
    internal fun reset(): Unit {
        initialized = false
    }
}
