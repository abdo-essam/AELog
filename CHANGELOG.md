# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `stability-config.conf` — targeted Compose compiler stability config (replaces unsafe wildcard)
- `NetworkMethod.UNKNOWN` — safe fallback for custom/unknown HTTP verbs; `fromString()` no longer silently maps to `GET`
- `androidMain` `callerTag()` — added Android-specific skip prefixes (`dalvik.`, `android.`) to stack walker

### Fixed
- **Critical:** `LogEntry` default parameter side-effects — `id` and `timestamp` no longer have defaults; `copy()` no longer silently generates a new ID and timestamp
- **Critical:** `RingBuffer.replace()` off-by-one — bound check was `0..size` (inclusive), now correctly `0 until size`
- **Critical:** `LogConfig.enabled` was dead code — now correctly gates logging in `LogRecorder` alongside `AELog.isEnabled`
- **Critical:** `ExperimentalTime` API no longer leaks into consumer API — suppressed at module level via `compilerOptions.freeCompilerArgs` in all 4 affected modules; `@file:OptIn` annotations removed from source files
- **High:** `LogInspector.record()` now fans out to **all** `LogRecordSink` plugins, not just the first
- **High:** Ktor interceptor response body read wrapped in `runCatching` — pipeline no longer breaks on read failure; error is recorded instead
- **High:** `AnalyticsEvent` changed from `@Immutable` to `@Stable` — `Map<String, Any>` cannot guarantee immutability; `properties` type changed to `Map<String, String>`
- **Medium:** `AnalyticsTracker` uses proper imports instead of fully-qualified names throughout
- **Medium:** `AELog.init()` — `LogInspector` constructed before CAS is now immediately GC-eligible on CAS loss (no live scopes)
- **Medium:** `NetworkMethod.valueOf()` crash on unknown HTTP verbs (CONNECT, TRACE, custom) in `NetworkRecorder` and `KtorInterceptor` — replaced with safe `fromString()`
- **Medium:** Race condition in `PluginManager.install()` — replaced TOCTOU check with `SynchronizedObject` lock ensuring `onAttach()` is never called twice
- `LogController.toggle()` non-atomic read-write — replaced with `MutableStateFlow.update { !it }`
- `LogConfig` KDoc referenced non-existent `AELog.create()` — corrected to `AELog.init()`
- `AnalyticsTracker` KDoc referenced non-existent `AELog.default` — corrected to `AELog.analytics` proxy
- README: version references updated from `1.0.0` to `1.0.2`
- README: `AELog.v()` direct calls corrected to `AELog.log.v()` to match actual API
- README: Modularity callout referenced wrong artifact names (`logs-network`) — corrected to `log-network`

### Changed
- `Lifecycle` class is now `internal` — it was unreachable by consumers but incorrectly exposed in binary API
- `RingBuffer` class is now `internal` — it is a pure implementation detail of `PluginStore`
- `AnalyticsTracker.track()` and `AnalyticsProxy.logEvent/logScreen` now accept `Map<String, String>` (was `Map<String, Any>`)
- `@Volatile` added to `viewModel` fields in `LogPlugin`, `NetworkPlugin`, and `AnalyticsPlugin`
- All modules: `explicitApiWarning()` → `explicitApi()` to enforce strict visibility for published library
- `log-network-ktor`: `ktor-client-core` changed from `implementation` to `api` — consumers need Ktor types transitively
- `log-network-okhttp`: `okhttp` changed from `implementation` to `api` — consumers need OkHttp types transitively
- `stability-config.conf` wildcard `com.ae.log.*` replaced with targeted entries for open filter classes only


## [1.0.2] - 2026-05-03

### Fixed
- Fixed lint errors in `LogProvider` and `OkHttpInterceptor`
- Resolved prefix naming consistency issues

## [1.0.1] - 2026-05-02

### Added
- Standardized `IdGenerator` across all modules
- Improved URL decoding for query parameters

## [1.0.0] - 2026-05-01

### Added
- 🎉 Initial release
- Core `AELog` engine with plugin architecture
- Built-in `LogPlugin` with search, filter, and copy
- Material3 themed UI with light/dark mode support
- Adaptive layout: bottom sheet (phones) / dialog (tablets)
- Floating debug button with configurable position
- Long-press gesture to open inspector
- `LogProvider` composable wrapper
- `LogConfig` for customization
- `UIPlugin` and `DataPlugin` interfaces for extensions
- KMP support: Android, iOS (arm64, x64, simulatorArm64)
- JSON syntax highlighting in log details
- HTTP method/status badge coloring
- Copy single log / copy all logs to clipboard
- Zero runtime overhead when `enabled = false`

### Architecture
- Instance-based design (no hidden globals)
- `StateFlow`-based reactive data layer
- Thread-safe `LogStore` with configurable max entries
- Plugin lifecycle: `onAttach → onOpen ⇄ onClose → onDetach`

[Unreleased]: https://github.com/abdo-essam/AELog/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/abdo-essam/AELog/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/abdo-essam/AELog/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/abdo-essam/AELog/releases/tag/v1.0.0
