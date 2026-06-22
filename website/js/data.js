/**
 * AELog Website — Centralized Data & Constants
 *
 * Single Source of Truth for all content, versions, and configuration.
 * Separates data from presentation logic (Separation of Concerns).
 */

// ── Version ────────────────────────────────────────────────────────────────
export const AELOG_VERSION = "1.1.7";

// ── Dependency Snippets (Step 1 of setup guide) ────────────────────────────
export const DEP_SNIPPETS = {
    logs: `// Logs only — ae-log-core included transitively
commonMain.dependencies {
    implementation("io.github.abdo-essam:ae-log-logs:${AELOG_VERSION}")
}`,

    "network-ktor": `// Network inspection with Ktor (KMP / Android / iOS)
// Includes ae-log-network and ae-log-core transitively
commonMain.dependencies {
    implementation("io.github.abdo-essam:ae-log-network-ktor:${AELOG_VERSION}")
}`,

    "network-okhttp": `// Network inspection with OkHttp (Android only)
// Includes ae-log-network and ae-log-core transitively
androidMain.dependencies {
    implementation("io.github.abdo-essam:ae-log-network-okhttp:${AELOG_VERSION}")
}`,

    analytics: `// Analytics event tracker — ae-log-core included transitively
commonMain.dependencies {
    implementation("io.github.abdo-essam:ae-log-analytics:${AELOG_VERSION}")
}`,

    crashes: `// Crash reporter — ae-log-core included transitively
commonMain.dependencies {
    implementation("io.github.abdo-essam:ae-log-crashes:${AELOG_VERSION}")
}`,

    full: `// Full stack: Logs + Network (Ktor) + Analytics + Crashes
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.abdo-essam:ae-log-logs:${AELOG_VERSION}")
            implementation("io.github.abdo-essam:ae-log-network-ktor:${AELOG_VERSION}")
            implementation("io.github.abdo-essam:ae-log-analytics:${AELOG_VERSION}")
            implementation("io.github.abdo-essam:ae-log-crashes:${AELOG_VERSION}")
        }
        androidMain.dependencies {
            // Optional: add only if your Android target also uses OkHttp
            implementation("io.github.abdo-essam:ae-log-network-okhttp:${AELOG_VERSION}")
        }
    }
}`,
};

// ── Architecture Node Info (Hover panel content) ────────────────────────────
export const ARCH_DATA = {
    core: {
        badge: "Core Engine",
        title: "AELog Core",
        desc: "The heartbeat of the system. Manages Plugin Registry, PluginContext lifecycle, UI rendering loop, and the global Singleton API.",
        code: `// Install plugins and toggle notch globally
AELog.install(LogPlugin())
AELog.showNotch = false`,
    },
    logs: {
        badge: "ae-log-logs",
        title: "LogPlugin",
        desc: "Real-time Logcat-style viewer with VERBOSE / DEBUG / INFO / WARN / ERROR filtering, search, and copy. Backed by InMemoryPluginStorage (RingBuffer).",
        code: `// Structured log API — mirrors Android Log.*
AELog.log.v("Auth", "Token checked")
AELog.log.d("Auth", "Token refreshed")
AELog.log.i("Home", "App launched!")
AELog.log.w("Auth", "Session expiring soon")
AELog.log.e("DB", "Write failed", exception)

// Auto-tag: omit the tag — derived from caller class
AELog.log.d("Token refreshed")  // tag → "AuthViewModel"`,
    },
    crashes: {
        badge: "ae-log-crashes",
        title: "CrashPlugin",
        desc: "Intercepts uncaught exceptions globally. Persists crash reports to PersistentPluginStorage so they survive app restarts and appear on next launch.",
        code: `// Automatically captures all uncaught exceptions.
// Record non-fatal exceptions manually:
AELog.crashes.recordNonFatal(exception)

// Read persisted crashes on next launch:
AELog.crashes.getCrashes()

// Clear stored crash reports:
AELog.crashes.clear()`,
    },
    network: {
        badge: "ae-log-network",
        title: "NetworkPlugin",
        desc: "Captures every HTTP request and response. Supports Ktor (multiplatform) and OkHttp (Android). Shows method badges, status codes, headers, and JSON payloads.",
        code: `// Ktor (KMP — all platforms)
val client = HttpClient {
  install(AELogKtorPlugin)
}

// OkHttp (Android only)
OkHttpClient.Builder()
  .addInterceptor(AELogOkHttpInterceptor())
  .build()

// Read captured requests:
AELog.network.getLogs()`,
    },
    analytics: {
        badge: "ae-log-analytics",
        title: "AnalyticsPlugin",
        desc: "Tracks analytics events, screen views, and custom properties. Displays them in a searchable panel with expandable property chips. Thread-safe, backed by InMemoryPluginStorage.",
        code: `// Log an analytics event
AELog.analytics.logEvent(
  name = "purchase",
  properties = mapOf(
    "item"  to "premium",
    "price" to "9.99"
  )
)

// Log a screen view
AELog.analytics.logScreen("HomeScreen")`,
    },
    ui: {
        badge: "Presentation Layer",
        title: "UI Overlay",
        desc: "The rendering overlay displaying the floating notch trigger and full inspector panel. Written entirely in Jetpack Compose Multiplatform for seamless cross-platform performance.",
        code: `// Overlay sits at the root of your UI tree
AELogOverlay(
    showNotch = true,
    initialTheme = Theme.Dark
)`,
    },
    ram: {
        badge: "In-Memory Storage",
        title: "RAM Storage",
        desc: "Ultra-fast, non-blocking cache powered by an efficient RingBuffer. Guarantees that logs, network data, and analytics events never consume disk IO during app execution.",
        code: `// Set maximum capacity (number of events)
AELog.install(LogPlugin(
    storage = InMemoryPluginStorage(capacity = 500)
))`,
    },
    disk: {
        badge: "Persistent Storage",
        title: "Disk Storage",
        desc: "Robust local persistence backed by platform-specific file systems (FileOperations). Used by CrashPlugin to ensure crash logs are stored safely and survive application restarts.",
        code: `// Stored locally in app Sandbox directory:
// Android: /data/data/app/files/aelog/
// iOS: Library/Application Support/aelog/`,
    },
    interceptors: {
        badge: "Auto-Interceptors",
        title: "Network Interceptors",
        desc: "Plugs directly into your network clients (Ktor or OkHttp) to automatically intercept and log HTTP traffic, including method badges, status codes, headers, and JSON body payloads.",
        code: `// Add Ktor interceptor globally:
val client = HttpClient {
    install(AELogKtorInterceptor)
}`,
    },
};

