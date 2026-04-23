package com.ae.devlens

import com.ae.devlens.core.DevLensPlugin
import com.ae.devlens.plugins.logs.LogsPlugin
import kotlinx.atomicfu.atomic

/**
 * One-stop setup helper for apps using the `:devlens` all-in-one dependency.
 *
 * ## Idempotent
 * [init] is safe to call multiple times — only the **first** call installs plugins.
 * `install()` deduplicates by plugin ID so even a concurrent double-init
 * just results in harmless no-op second installs.
 *
 * ```kotlin
 * DevLensSetup.init()   // installs LogsPlugin
 * DevLensSetup.init()   // no-op — returns same instance immediately
 * ```
 *
 * ## Custom plugins
 * ```kotlin
 * DevLensSetup.init(
 *     config  = DevLensConfig(),
 *     plugins = listOf(LogsPlugin(maxEntries = 2000), NetworkPlugin(), AnalyticsPlugin()),
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
    private val initialized = atomic(false)

    /**
     * Initialize DevLens. **Idempotent** — safe to call multiple times.
     *
     * Only the first call installs [plugins] onto [AEDevLens.default].
     * Subsequent calls return immediately with the same instance.
     *
     * @param config   Core configuration (only applied on first call).
     * @param plugins  Plugins to install. Defaults to [LogsPlugin] only.
     * @return The shared [AEDevLens.default] instance.
     */
    public fun init(
        config: DevLensConfig = DevLensConfig(),
        plugins: List<DevLensPlugin> = listOf(LogsPlugin()),
    ): AEDevLens {
        if (!initialized.compareAndSet(expect = false, update = true)) {
            return AEDevLens.default
        }

        val inspector = AEDevLens.default
        plugins.forEach { inspector.install(it) } // install() deduplicates by plugin ID
        return inspector
    }
}
