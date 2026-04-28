<p align="center">
  <img src="docs/assets/logo.png" width="600" alt="AELogs Banner" />
</p>

<h1 align="center">AELogs</h1>

<p align="center">
  <strong>Extensible on-device dev tools for Kotlin Multiplatform</strong>
  <br />
  A mini Flipper for KMP — inspect logs, network, and more with a beautiful Compose UI.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.abdo-essam/logs">
    <img src="https://img.shields.io/maven-central/v/io.github.abdo-essam/logs?style=flat-square&color=BF3547" alt="Maven Central" />
  </a>
  <a href="https://github.com/abdo-essam/AELogs/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/abdo-essam/AELogs/ci.yml?branch=main&style=flat-square" alt="CI" />
  </a>
  <a href="https://codecov.io/gh/abdo-essam/AELogs">
    <img src="https://img.shields.io/codecov/c/github/abdo-essam/AELogs?style=flat-square&color=00B894" alt="Code Coverage" />
  </a>
  <a href="https://kotlin.github.io/binary-compatibility-validator/">
    <img src="https://img.shields.io/badge/API-stable-blue?style=flat-square" alt="API Stability" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/abdo-essam/AELogs?style=flat-square" alt="License" />
  </a>
  <a href="https://kotlinlang.org">
    <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin" />
  </a>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-installation">Installation</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-plugins">Plugins</a> •
  <a href="#-custom-plugins">Custom Plugins</a> •
  <a href="https://abdo-essam.github.io/AELogs/">Documentation</a>
</p>

---

<p align="center">
  <img src="docs/assets/demo-light.gif" width="280" alt="Light Mode Demo" />
  &nbsp;&nbsp;&nbsp;
  <img src="docs/assets/demo-dark.gif" width="280" alt="Dark Mode Demo" />
</p>

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔍 **Log Inspector** | Search, filter, and copy logs with syntax-highlighted JSON |
| 🌐 **Network Viewer** | HTTP request/response inspection with method badges |
| 📊 **Analytics Tracker** | Monitor analytics events in real-time |
| 🎨 **Beautiful UI** | Material3 design with light/dark mode support |
| 🧩 **Plugin System** | Extend with custom debug panels through modular dependencies |
| 📱 **Adaptive Layout** | Bottom sheet on phones, dialog on tablets |
| 🔌 **Zero Release Overhead**| Disable with a single flag — no runtime cost |
| 🍎 **Multiplatform** | Android, iOS, Desktop (JVM), Web (WASM) |

## 📦 Installation

AELogs is fully modularized. Include only the plugins you need to keep your app light!

### Kotlin Multiplatform

```kotlin
// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core: inspector UI + LogsPlugin
            implementation("io.github.abdo-essam:logs:1.0.0")

            // Optional: Network inspector panel
            implementation("io.github.abdo-essam:logs-network:1.0.0")

            // Optional: Ktor auto-interceptor (KMP — Android / iOS / JVM)
            implementation("io.github.abdo-essam:logs-network-ktor:1.0.0")

            // Optional: Analytics inspector panel
            implementation("io.github.abdo-essam:logs-analytics:1.0.0")
        }
        androidMain.dependencies {
            // Optional: OkHttp auto-interceptor (Android + JVM only)
            implementation("io.github.abdo-essam:logs-network-okhttp:1.0.0")
        }
    }
}
```

> **🔥 True Modularity**: Worried about APK/bundle size? You can drop the `LogsPlugin` entirely! Just remove the `logs` dependency and import `logs-network:1.0.0` or `logs-analytics:1.0.0` directly. Because each acts completely independently and transitively exports `logs-core` and `logs-ui`, your app stays incredibly lightweight!

### Version Catalog

```toml
[versions]
logs = "1.0.0"

[libraries]
logs-core              = { module = "io.github.abdo-essam:logs",                version.ref = "logs" }
logs-network           = { module = "io.github.abdo-essam:logs-network",        version.ref = "logs" }
logs-network-ktor      = { module = "io.github.abdo-essam:logs-network-ktor",   version.ref = "logs" }
logs-network-okhttp    = { module = "io.github.abdo-essam:logs-network-okhttp", version.ref = "logs" }
logs-analytics         = { module = "io.github.abdo-essam:logs-analytics",      version.ref = "logs" }
```

## 🚀 Quick Start

### 1. Initialize & Install Plugins

Best called early in your platform-specific entry points (e.g. `Application.onCreate` for Android, or main `ViewController` for iOS):

```kotlin
AELogs.init(
    LogsPlugin(),      // Built-in log viewer
    NetworkPlugin(),   // Network inspector
    AnalyticsPlugin()  // Analytics tracker
)
```

### 2. Wrap Your App with UI Provider

```kotlin
@Composable
fun App(debugMode: Boolean) {
    AELogsProvider(
        enabled = debugMode, // ← disables UI overhead in release builds
        uiConfig = AELogsUiConfig(
            showFloatingButton = true, // Enables the 'bug' overlay button
            enableLongPress = true,    // Show panel on 3-finger long press
        )
    ) {
        MaterialTheme {
            YourAppContent()
        }
    }
}
```

### 3. Log — primary API (`AELog`)

`AELog` is a discoverable object modelled after Android's built-in `Log` class.
Just type `AELog.` and the IDE lists every method — no extension hunting required:

```kotlin
AELog.v("Auth", "Token checked")
AELog.d("Auth", "Token refreshed")
AELog.i("HomeScreen", "App launched!")
AELog.w("Auth", "Session expiring soon")
AELog.e("Database", "Failed to clear cache", exception) // stack trace auto-appended
AELog.wtf("Auth", "Unexpected state")
```

> All calls are **silent no-ops** if `AELogs.init()` has not been called yet — safe in shared modules that run before app startup.

#### Tagged logger — eliminate tag repetition (recommended for classes)

Create one `TaggedLogger` per class. The tag is set once and applied to every call:

```kotlin
class AuthViewModel {
    // ✅ Tag declared once — never repeated
    private val log = AELog.logger("AuthViewModel")
    // or: private val log = AELogs.logger("AuthViewModel")

    fun login() {
        log.d("Login started")
        log.i("OTP verified")
        log.e("Token refresh failed", throwable)
    }
}
```

#### Companion shorthands (alternative — same result)

```kotlin
// If you already have AELogs imported, these are identical to AELog.*
AELogs.d("Auth", "Token refreshed")
AELogs.e("Database", "Failed to clear cache", exception)
```

```kotlin
// Network & Analytics APIs
NetworkApi.logRequest(method = "GET", url = "https://api.example.com/users", headers = mapOf("Auth" to "Bearer 123"))
NetworkApi.logResponse(url = "https://api.example.com/users", statusCode = 200, responseBody = "{ \"count\": 2 }")
AnalyticsApi.logEvent("item_added_to_cart", properties = mapOf("id" to "123", "price" to "29.99"))
```

### 4. Open AELogs

Three ways to open the inspector:
1. Tap the floating **bug button** (bottom-right corner)
2. Long-press with multiple fingers anywhere on screen (if enabled)
3. Programmatically: `LocalAELogsController.current.show()`

## 🧩 Modularity & Available Plugins

| Module / Plugin | Class | Description |
|--------|------|-------------|
| `:logs` | `LogsPlugin` | Log viewer with severity filters (ALL / VERBOSE / DEBUG / INFO / WARN / ERROR) |
| `:logs-network` | `NetworkPlugin` | HTTP inspector with method badges, status filtering (2xx / 4xx / 5xx) and full body view |
| `:logs-analytics` | `AnalyticsPlugin` | Analytics tracker separating Screens / Events with expandable properties |

## 🔨 Custom Plugins

Create your own debug panel (e.g., a Database Inspector or Feature Flags toggler) in 3 steps:

```kotlin
class FeatureFlagsPlugin : UIPlugin {
    override val id = "feature_flags"
    override val name = "Flags"
    override val icon = Icons.Default.Flag

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    override fun onAttach(context: PluginContext) {
        // Initialize your plugin (observe context.scope, context.eventBus, etc.)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        // Your Compose UI here
        LazyColumn(modifier = modifier) {
            items(flags) { flag ->
                FlagRow(flag)
            }
        }
    }
}

// Install it
AELogs.init(LogsPlugin(), FeatureFlagsPlugin())
```

📖 See the [Custom Plugins Guide](https://abdo-essam.github.io/AELogs/custom-plugins) for the full API reference.

## 🔗 Logging Integrations

AELogs works seamlessly with your existing logging infrastructures (like Kermit or Napier). Just forward your logs to the APIs.

```kotlin
class AELogsKermitWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        // Pass throwable directly — stack trace is appended automatically by AELogs
        AELogs.log(
            severity = severity.toAELogsLogSeverity(),
            tag = tag,
            message = message,
            throwable = throwable,
        )
    }
}
```

📖 See the [Logging Integrations Guide](https://abdo-essam.github.io/AELogs/integrations) for more examples.

## 🏗️ Architecture

The SDK follows an encapsulated `Model-Store-API-UI` pattern, making plugins 100% reactive, modular, and thread-safe.

```text
┌─────────────────────────────────────────────────┐
│              AELogsProvider                   │  Compose wrapper
│  ┌───────────────────────────────────────────┐  │
│  │            AELogs (Core)               │  │  Plugin engine
│  │  ┌─────────┐ ┌─────────┐ ┌────────────┐  │  │
│  │  │  Logs   │ │ Network │ │ Analytics  │  │  │  Plugins
│  │  │ Plugin  │ │ Plugin  │ │  Plugin    │  │  │
│  │  └────┬────┘ └────┬────┘ └─────┬──────┘  │  │
│  │       │           │            │          │  │
│  │  ┌────┴────┐ ┌────┴────┐ ┌────┴──────┐  │  │
│  │  │LogStore │ │NetStore │ │AnalyticsStore│  │ Data layer
│  │  └─────────┘ └─────────┘ └───────────┘  │  │
│  └───────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## 📋 Requirements

| Platform | Minimum Version |
|----------|----------------|
| Android | API 24 (Android 7.0) |
| iOS | 15.0 |
| Kotlin | 2.1.10+ |
| Compose Multiplatform | 1.7.3+ |

## 🤝 Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) first.

```bash
git clone https://github.com/abdo-essam/AELogs.git
cd AELogs
./gradlew build
./gradlew allTests
```

## 📄 License

```text
Copyright 2026 Abdo Essam

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```

## 💖 Acknowledgements

- Jetpack Compose — UI toolkit
- Kotlin Multiplatform — Cross-platform
- Flipper — Inspiration for the plugin architecture
- Chucker — Inspiration for network inspection
