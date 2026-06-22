<h1 align="center">
  <img src="website/img/lens_logo.svg" width="38" height="38" alt="AELog Logo" style="vertical-align: middle; margin-right: 10px;" /><span style="vertical-align: middle; position: relative;">AELog</span>
</h1>

<p align="center">
  <strong>Extensible on-device dev tools for Kotlin Multiplatform</strong>
  <br />
  An in-app debugging overlay for KMP — inspect logs, network traffic, and analytics events with a beautiful Compose UI. No external tools needed.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.abdo-essam/ae-log-logs">
    <img src="https://img.shields.io/maven-central/v/io.github.abdo-essam/ae-log-logs?style=flat-square&color=BF3547" alt="Maven Central" />
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
  <img src="website/img/aelog_logs.png" width="190" alt="Logs Plugin" />
  &nbsp;
  <img src="website/img/aelog_network.png" width="190" alt="Network Plugin" />
  &nbsp;
  <img src="website/img/aelog_analytics.png" width="190" alt="Analytics Plugin" />
  &nbsp;
  <img src="website/img/aelog_crashes.png" width="190" alt="Crashes Plugin" />
</p>

## ✨ Core Plugins

AELog provides a suite of 4 core plugins, allowing you to select and install only what you need:

| Plugin | Purpose | Key Capabilities |
|:---|:---|:---|
| 🔍 **Log Inspector** | On-Device Log Viewer | Search queries, filter by severity level/tag, and copy or share logs easily. |
| 🌐 **Network Viewer** | HTTP Traffic Inspector | Inspect HTTP requests and responses, full headers, status codes, and JSON payloads with automatic sensitive credential redaction. |
| 📊 **Analytics Tracker** | Analytics Event Tracker | Verify custom properties, event dispatches, and screen views instantly as they trigger in your app. |
| 💥 **Crash Reporter** | Local Exception Manager | Intercept fatal exceptions and record non-fatal errors on-device so they survive app restarts and are viewable in the UI. |

## 📦 Installation

AELog is fully modularized. **Add only the dependencies you need.** Every plugin module carries its dependencies transitively, so you never need to import `ae-log-core` manually.

### 1. Version Catalog (Recommended)

Add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
aelog = "1.1.7"

[libraries]
aelog-logs             = { module = "io.github.abdo-essam:ae-log-logs",           version.ref = "aelog" }
aelog-network-ktor     = { module = "io.github.abdo-essam:ae-log-network-ktor",   version.ref = "aelog" }
aelog-network-okhttp   = { module = "io.github.abdo-essam:ae-log-network-okhttp", version.ref = "aelog" }
aelog-analytics        = { module = "io.github.abdo-essam:ae-log-analytics",      version.ref = "aelog" }
aelog-crashes          = { module = "io.github.abdo-essam:ae-log-crashes",        version.ref = "aelog" }
```

### 2. Gradle Setup

Add the required dependencies to your target source sets in `build.gradle.kts`:

```kotlin
// build.gradle.kts (shared module)
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Pick only what you need (each carries core transitively)
            implementation(libs.aelog.logs)
            implementation(libs.aelog.network.ktor)
            implementation(libs.aelog.analytics)
            implementation(libs.aelog.crashes)
        }
        androidMain.dependencies {
            // Add only if your Android target uses OkHttp
            implementation(libs.aelog.network.okhttp)
        }
    }
}
```

---

📖 See the [Full Installation Guide](https://abdo-essam.github.io/AELog/) for direct dependency coordinates and details on transitive inclusions.

## 🚀 Quick Start

### 1. Zero-Config (Automatic Setup)
AELog features **zero-config auto-initialisation** on Android and iOS! Just add the Gradle dependencies for the plugins you want, and AELog automatically boots up with sensible defaults when your app launches. **No setup, configuration, or initialization code is required.**

### 2. Drop in the Overlay

#### For Compose Apps (Android & iOS)
Add `AELogOverlay()` as a **sibling** anywhere in your root composable — no wrapping required. By default, the floating notch trigger is enabled (`showNotch = true`), allowing you to tap it to open the inspector:

```kotlin
@Composable
fun App() {
    // Renders the overlay container in the background
    AELogOverlay() 
    
    MaterialTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { AELog.show() }) {
                    Icon(Icons.Default.BugReport, contentDescription = "Open Inspector")
                }
            }
        ) {
            YourAppContent()
        }
    }
}
```

To **disable the floating notch trigger** globally or locally:
```kotlin
AELog.showNotch = false // Disable globally (across all screens)
// or
AELogOverlay(showNotch = false) // Disable locally in this composable
```

To **disable the library entirely in release builds**, set:
```kotlin
AELog.isEnabled = BuildConfig.DEBUG
```

### 4. Log — primary API (`AELog`)

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

> All calls are **silent no-ops** if the library hasn't initialized yet — safe to call from shared modules before app startup.

#### Auto-tag — no tag required (recommended)

Omit the tag and AELog derives it from the caller's class name automatically. No repetition, no overhead:

```kotlin
AELog.log.d("Token refreshed")          // tag → "AuthViewModel"
AELog.log.i("App launched!")             // tag → "HomeScreen"
AELog.log.e("Failed to clear cache", t)  // tag → "Database"
```

```kotlin
// Network, Analytics & Crashes APIs
AELog.network.logRequest(method = "GET", url = "https://api.example.com/users")
AELog.network.logResponse(url = "https://api.example.com/users", statusCode = 200)
AELog.analytics.logEvent("item_added_to_cart", properties = mapOf("id" to "123"))

// Capture non-fatal exceptions manually
try {
    performDangerousWork()
} catch (t: Throwable) {
    AELog.crashes.recordNonFatal(t)
}
```

### 🌐 Network Interceptors

AELog provides first-class interceptors for OkHttp and Ktor.

#### Security (Header Exclusion)
Both interceptors are **secure by default**. They automatically exclude sensitive headers like `Authorization` and `Cookie` to prevent credentials from appearing in logs.

```kotlin
// OkHttp
val interceptor = AELogOkHttpInterceptor(
    excludeHeaders = setOf("X-Sensitive-Header") // Extends default exclusions
)

// Ktor
val client = HttpClient {
    install(AELogKtorInterceptor) {
        excludeHeaders = setOf("X-Api-Key")
    }
}
```

#### Body Truncation (OOM Prevention)
To prevent memory issues when inspecting large payloads (e.g., file uploads), bodies are automatically truncated (default 250 KB).

```kotlin
AELogOkHttpInterceptor(
    maxRequestBodyBytes = 500_000,  // 500 KB limit
    maxResponseBodyBytes = 1_000_000 // 1 MB limit
)
```

#### Ktor Response Body Capture
By default, Ktor response streams can only be read once. To enable non-destructive inspection of response bodies:
1. **Install DoubleReceive**: It is highly recommended to install the `DoubleReceive` plugin in your `HttpClient`.
2. **Integrated Fallback**: AELog will attempt to capture the body using Ktor's internal stream handlers. If `DoubleReceive` is not installed, this may consume the stream—ensure your app logic is compatible or use the recommended plugin.

```kotlin
val client = HttpClient {
    install(DoubleReceive) // Recommended for Network Plugin
    install(AELogKtorInterceptor)
}
```

### 5. Open AELog

Three ways to open the inspector:
1. Tap the **floating notch** at the top of the screen (Dynamic Island-style)
2. Programmatically from anywhere: `AELog.show()` / `AELog.hide()`
3. Wire it to any custom trigger (shake gesture, debug menu button, etc.)

## 🔨 Custom Plugins

Create your own debug panel (e.g., a Database Inspector or Feature Flags toggler) in 3 steps:

```kotlin
class FeatureFlagsPlugin : UIPlugin {
    override val name = "Flags"

    @Composable
    override fun Content(modifier: Modifier) {
        // Your Compose UI here (owns the entire panel layout)
        LazyColumn(modifier = modifier) {
            items(flags) { flag ->
                FlagRow(flag)
            }
        }
    }
}

// Install your custom plugin alongside the auto-registered ones
AELog.install(FeatureFlagsPlugin())
```

📖 See the [Custom Plugins Guide](https://abdo-essam.github.io/AELog/custom-plugins) for the full API reference.

## 🔗 Logging Integrations

AELog works with **any** logging library. Just forward logs to the static `AELog.log` methods:

```kotlin
// Forward logs using the static shorthands directly
AELog.log.i("MyTag", "Something happened")
AELog.log.e("Database", "Failed to clear cache", exception)
```

📖 See the [Logging Integrations Guide](https://abdo-essam.github.io/AELog/integrations) for adapter examples (Kermit, Napier, Timber, SLF4J).

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

