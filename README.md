<h1 align="center">AELog</h1>

<p align="center">
  <strong>Extensible on-device dev tools for Kotlin Multiplatform</strong>
  <br />
  An in-app debugging overlay for KMP — inspect logs, network traffic, and analytics events with a beautiful Compose UI. No external tools needed.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.abdo-essam/logs">
    <img src="https://img.shields.io/maven-central/v/io.github.abdo-essam/logs?style=flat-square&color=BF3547" alt="Maven Central" />
  </a>
  <a href="https://github.com/abdo-essam/AELog/actions/workflows/ci.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/abdo-essam/AELog/ci.yml?branch=main&style=flat-square" alt="CI" />
  </a>
  <a href="https://codecov.io/gh/abdo-essam/AELog">
    <img src="https://img.shields.io/codecov/c/github/abdo-essam/AELog?style=flat-square&color=00B894" alt="Code Coverage" />
  </a>
  <a href="https://kotlin.github.io/binary-compatibility-validator/">
    <img src="https://img.shields.io/badge/API-stable-blue?style=flat-square" alt="API Stability" />
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/github/license/abdo-essam/AELog?style=flat-square" alt="License" />
  </a>
  <a href="https://kotlinlang.org">
    <img src="https://img.shields.io/badge/Kotlin-2.2.0-7F52FF?style=flat-square&logo=kotlin" alt="Kotlin" />
  </a>
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-installation">Installation</a> •
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-plugins">Plugins</a> •
  <a href="#-custom-plugins">Custom Plugins</a> •
  <a href="https://abdo-essam.github.io/AELog/">Documentation</a>
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

AELog is fully modularized. Include only the plugins you need to keep your app light!

### Kotlin Multiplatform

```kotlin
// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core: inspector UI + LogPlugin
            implementation("io.github.abdo-essam:log:1.0.3")

            // Optional: Network inspector panel
            implementation("io.github.abdo-essam:log-network:1.0.3")

            // Optional: Ktor auto-interceptor (KMP — Android / iOS / JVM)
            implementation("io.github.abdo-essam:log-network-ktor:1.0.3")

            // Optional: Analytics inspector panel
            implementation("io.github.abdo-essam:log-analytics:1.0.3")
        }
        androidMain.dependencies {
            // Optional: OkHttp auto-interceptor (Android + JVM only)
            implementation("io.github.abdo-essam:log-network-okhttp:1.0.3")
        }
    }
}
```

> **🔥 True Modularity**: You can depend on any plugin module independently! For example, if you only need network inspection, import `log-network:1.0.3` directly — it transitively brings in `log-core`. Your app stays lightweight by only including what it needs.

### Version Catalog

```toml
[versions]
logs = "1.0.3"

[libraries]
logs-core              = { module = "io.github.abdo-essam:log",                version.ref = "logs" }
logs-network           = { module = "io.github.abdo-essam:log-network",        version.ref = "logs" }
logs-network-ktor      = { module = "io.github.abdo-essam:log-network-ktor",   version.ref = "logs" }
logs-network-okhttp    = { module = "io.github.abdo-essam:log-network-okhttp", version.ref = "logs" }
logs-analytics         = { module = "io.github.abdo-essam:log-analytics",      version.ref = "logs" }
```

## 🚀 Quick Start

### 1. Initialize & Install Plugins

Best called early in your platform-specific entry points (e.g. `Application.onCreate` for Android, or main `ViewController` for iOS):

```kotlin
AELog.init(
    LogPlugin(),      // Built-in log viewer
    NetworkPlugin(),   // Network inspector
    AnalyticsPlugin()  // Analytics tracker
)
```

### 2. Launch the UI

#### For Jetpack Compose Apps
Wrap your main content:

```kotlin
@Composable
fun App(debugMode: Boolean) {
    LogProvider(
        enabled = debugMode, // ← disables UI overhead in release builds
        uiConfig = UiConfig(
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

#### For Traditional View-Based Android Apps (XML)
No need to add Compose to your app! Just launch the built-in activity anywhere (like a developer menu button):

```kotlin
import com.ae.log.launchViewer
import com.ae.log.AELog

// Call from any Activity or Fragment
AELog.launchViewer(requireContext()) 
```

### 3. Log — primary API (`AELog`)

`AELog` is a discoverable object modelled after Android's built-in `Log` class.
Just type `AELog.` and the IDE lists every method — no extension hunting required:

```kotlin
AELog.log.v("Auth", "Token checked")
AELog.log.d("Auth", "Token refreshed")
AELog.log.i("HomeScreen", "App launched!")
AELog.log.w("Auth", "Session expiring soon")
AELog.log.e("Database", "Failed to clear cache", exception) // stack trace auto-appended
AELog.log.wtf("Auth", "Unexpected state")
```

> All calls are **silent no-ops** if `AELog.init()` has not been called yet — safe in shared modules that run before app startup.

#### Auto-tag — no tag required (recommended)

Omit the tag and AELog derives it from the caller's class name automatically. No repetition, no overhead:

```kotlin
AELog.log.d("Token refreshed")          // tag → "AuthViewModel"
AELog.log.i("App launched!")             // tag → "HomeScreen"
AELog.log.e("Failed to clear cache", t)  // tag → "Database"
```

```kotlin
// Network & Analytics APIs
AELog.network.logRequest(method = "GET", url = "https://api.example.com/users")
AELog.network.logResponse(url = "https://api.example.com/users", statusCode = 200)
AELog.analytics.logEvent("item_added_to_cart", properties = mapOf("id" to "123"))
```

### 🌐 Network Interceptors

AELog provides first-class interceptors for OkHttp and Ktor.

#### Security (Redaction)
Both interceptors are **secure by default**. They automatically redact sensitive headers like `Authorization` and `Cookie` to prevent token leakage in logs.

```kotlin
// OkHttp
val interceptor = OkHttpInterceptor(
    redactHeaders = setOf("X-Sensitive-Header") // Extends default redactions
)

// Ktor
val client = HttpClient {
    install(KtorInterceptor) {
        redactHeaders = setOf("X-Api-Key")
    }
}
```

#### Body Truncation (OOM Prevention)
To prevent memory issues when inspecting large payloads (e.g., file uploads), bodies are automatically truncated (default 250 KB).

```kotlin
OkHttpInterceptor(
    maxRequestBodyBytes = 500_000,  // 500 KB limit
    maxResponseBodyBytes = 1_000_000 // 1 MB limit
)
```

#### Ktor Response Body Capture
By default, Ktor response streams can only be read once. To enable non-destructive inspection of response bodies:
1. **Install DoubleReceive**: It is highly recommended to install the `DoubleReceive` plugin in your `HttpClient`.
2. **Integrated Fallback**: AELog will attempt to capture the body using `bodyAsText()`. If `DoubleReceive` is not installed, this may consume the stream—ensure your app logic is compatible or use the recommended plugin.

```kotlin
val client = HttpClient {
    install(DoubleReceive) // Recommended for Network Plugin
    install(KtorInterceptor)
}
```

### 4. Open AELog

Three ways to open the inspector:
1. Tap the floating **bug button** (bottom-right corner)
2. Long-press with multiple fingers anywhere on screen (if enabled)
3. Programmatically: `LocalLogController.current.show()`

## 🧩 Modularity & Available Plugins

| Module / Plugin | Class | Description |
|--------|------|-------------|
| `:log` | `LogPlugin` | Log viewer with severity filters (ALL / VERBOSE / DEBUG / INFO / WARN / ERROR) |
| `:log-network` | `NetworkPlugin` | HTTP inspector with method badges, status filtering (2xx / 4xx / 5xx) and full body view |
| `:log-analytics` | `AnalyticsPlugin` | Analytics tracker separating Screens / Events with expandable properties |

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
AELog.init(LogPlugin(), FeatureFlagsPlugin())
```

📖 See the [Custom Plugins Guide](https://abdo-essam.github.io/AELog/custom-plugins) for the full API reference.

## 🔗 Logging Integrations

AELog works with **any** logging setup — just forward your log calls to the AELog API:

```kotlin
// Forward any log call directly — no bridge library needed
AELog.log(
    severity = LogSeverity.INFO,
    tag = "MyTag",
    message = "Something happened",
    throwable = null, // stack trace is appended automatically when present
)
```

📖 See the [Logging Integrations Guide](https://abdo-essam.github.io/AELog/integrations) for adapter examples (Kermit, Napier, Timber, SLF4J).

## 🏗️ Architecture

The SDK follows an encapsulated `Model-Store-API-UI` pattern, making plugins 100% reactive, modular, and thread-safe.

```text
┌─────────────────────────────────────────────────┐
│              LogProvider                   │  Compose wrapper
│  ┌───────────────────────────────────────────┐  │
│  │            AELog (Core)               │  │  Plugin engine
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
| Kotlin | 2.2.0+ |
| Compose Multiplatform | 1.7.3+ |

## 🤝 Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) first.

```bash
git clone https://github.com/abdo-essam/AELog.git
cd AELog
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
- Kotlin Multiplatform — Cross-platform framework
