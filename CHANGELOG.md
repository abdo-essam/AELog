# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Global Notch Toggle**: Added static property `AELog.showNotch` to allow globally enabling/disabling the floating trigger notch across all screens.
- **Zero-Config iOS & Android Auto-Init**: Synchronized documentation and setups to highlight zero-config auto-initialization on both Android and iOS targets.

### Removed
- **AELog.configure & LogConfig**: Removed the configure DSL block and `LogConfig` class, enforcing zero-config sensible defaults for all plugins.
- **onMigrateFrom Lifecycle Hook**: Removed the obsolete `onMigrateFrom(oldPlugin)` hook from the `Plugin` interface and its overrides in built-in plugins (`LogPlugin`, `NetworkPlugin`, `AnalyticsPlugin`) due to the deprecation of dynamic configuration hot-swaps.

---

## [1.1.5] - 2026-06-03

### Fixed
- **Compose Multiplatform MaterialTheme Linkage Error:** Removed all references to standard `MaterialTheme.colorScheme` and `MaterialTheme.typography` properties across core and plugins, replacing them with a custom static `LogTheme`. This completely prevents `IrLinkageError` crashes for `MaterialTheme$stable` on iOS when linking AELog into applications running mismatched Compose Multiplatform versions.

---

## [1.1.4] - 2026-06-03

### Fixed
- **Compose Multiplatform 1.9.x Compatibility:** Upgraded AELog to target Compose Multiplatform 1.9.3, Kotlin 2.3.0, and Ktor 3.3.0. This resolves `kotlin.internal.IrLinkageError` crashes when consuming AELog in projects using Compose Multiplatform 1.9.x due to `MaterialTheme$stable` field changes in the runtime.

---

## [1.1.3] - 2026-06-02

### Fixed
- **Compose Multiplatform Binary Compatibility:** Removed internal `CompositionLocalProvider` usage from the AELog UI layer. This prevents Kotlin/Native from emitting IR accesses to `ProvidedValue$stable` which was made private in Compose Multiplatform 1.8+. AELog is now fully binary-compatible with apps using both older (1.7.x) and newer (1.8+, 1.9.x) Compose Multiplatform runtime versions without requiring recompilation or raising `IrLinkageError`s.

---

## [1.1.2] - 2026-06-02

### Added
- **Zero-Config iOS Auto-Initialization:** Added automatic initialization for all core plugins (`Logs`, `Network`, `Analytics`, and `Crashes`) on iOS via `@EagerInitialization` in KMP. iOS consumers no longer need to write any configuration or setup code—mirroring Android's zero-code setup experience.
- **Auto-Initialization Documentation:** Fully documented iOS auto-initialization behaviors, including custom configuration and opt-out scenarios via the idempotent configuration API.

### Fixed
- **Ktor Raw Request Payloads:** Fixed interceptor logic in `log-network-ktor` to gracefully capture raw `String` and `ByteArray` request bodies when `ContentNegotiation` is not installed on the HttpClient.
- **LogNotchButton Rotated Text Clipping:** Fixed a UI layout bug on iOS where the rotated notification notch text was clipped or invisible by removing the static `.requiredWidth` constraint and applying `softWrap = false`.

---

## [1.1.1] - 2026-06-01

### Added
- **Request Body on Error:** Network logs now capture and display the request body early in the request pipeline, ensuring it remains visible even when requests fail with client/server errors or network exceptions.
- **Smart Binary Upload Handling:** Binary uploads (like images/documents via `multipart/form-data`) are no longer dumped as raw binary strings into logs. AELog now displays a clean summary like `<image/png: 110370 bytes>`, preventing memory bloat and UI lag.

### Changed
- **Premium Code Block Styles:** Request and response body sections in the network details view now render with a solid, clean white background and black text in light theme for maximum readability.
- **Enhanced Contrast for Success Responses:** Successful network entry detail sections are highlighted with a subtle green tint (`Color(0xFF4CAF50).copy(alpha = 0.08f)`) to distinguish them from the sheet background.

---

## [1.1.0] - 2026-06-01

### Added
- **DeviceInfo in crash reports:** Crash events now capture a `DeviceInfo` snapshot at crash time (device model, OS version, app version, build number). When a QA tester taps **Copy** on any crash, the clipboard text now includes a `── Device Info ──` section with full context — no need to ask the developer which build or device was affected.
- `DeviceInfo` `expect/actual` implemented for Android (`android.os.Build` + `PackageManager`), iOS (`UIDevice` + `NSBundle`), and JVM (system properties fallback).

---

## [1.0.9] - 2026-05-31

### Removed
- **EventBus & lifecycle signals (YAGNI):** The `EventBus`, `Event`, and `AELogLifecycle` classes have been deleted. None of the built-in plugins consumed these signals, so they were speculative plumbing that added unnecessary complexity. The `collectEvents` extension on `PluginContext` and the `eventBus` property are also gone. If lifecycle hooks are needed in the future they will be reintroduced based on a concrete use case.

### Changed
- `AELog.clearAll()` now delegates directly to `PluginManager` — the intermediate `AELogLifecycle.clearAll()` indirection is gone.
- `PluginContext` is now simpler: only `scope`, `config`, and `getPlugin()` — no event infrastructure.

---

## [1.0.8] - 2026-05-31

### Fixed
- **Ktor request body not logged for JSON POST requests:** When `ContentNegotiation` serializes a data class, Ktor produces a `WriteChannelContent` (stream). The interceptor now drains the stream into a byte array, logs the decoded JSON body, and re-wraps it as `ByteArrayContent` — so the actual HTTP request body is fully preserved and the correct JSON appears in the Network inspector.

### Tests
- Added `records JSON request body` test asserting that a `setBody(...)` POST with `ContentType.Application.Json` records the body content in the log export.
- Extended `records POST request` with body assertions (`name`, `test`) to prevent regression.

## [1.0.7] - 2026-05-29

### Simplified
- **Unified Plugin Architecture:** Consolidated the redundant `DataPlugin` marker interface and `UIPlugin` under a single, streamlined `Plugin` base contract.
- **Auto-Generated IDs:** Added reflection-based qualified class name `id` defaults, eliminating manual ID registration boilerplate.
- **Consolidated UI Layout:** Removed legacy layout slots (`HeaderContent()` and `HeaderActions()`) from `UIPlugin`. Plugins now have 100% layout control (like sticky search/headers) directly inside `Content()`.
- **YAGNI Lifecycle Cleanups:** Purged unused imperative hooks (`onStart()`, `onStop()`, `onOpen()`, `onClose()`) from the plugin interface in favor of reactive event subscriptions on the shared `EventBus`.

## [1.0.6] - 2026-05-28

### Added
- **Unified Configuration DSL:** Replaced legacy, fragmented initialization methods (`configureCore`, `configurePlugin`) with a robust, type-safe, and atomic **`AELog.configure { ... }`** builder DSL.
- **Global Notch Configuration:** Added `showNotch` parameter to `LogConfig` and `AELogBuilder` to allow developers to completely disable the floating notch trigger globally (affecting both XML Views and Compose setups) during startup.
- **High-Fidelity Edge-Docked Notch:** Redesigned the floating trigger notch from a top-center pill into a premium vertical button docked snug against the **right edge of the screen** (`Alignment.CenterEnd`), featuring edge-cropped corner rounding and a vertical glowing silver-white gradient accent bar on its outer boundary.

### Fixed
- **Overlay Layout Clipping:** Resolved the `0x0` parent measurement constraint bug in `AELogOverlay.kt` by applying `Modifier.fillMaxSize()` to the container. The parent bounds now fill the full screen, ensuring perfect edge docking of the notch trigger while maintaining a **100% invisible, touch-pass-through layout** that never intercepts underlying app interactions.
- **Removed Obsolete APIs:** Completely purged all deprecated legacy configuration methods (`configureCore`, `configurePlugin`) and old UI wrappers (such as `LogProvider` and `UiConfig`) to ensure a pristine, high-integrity, pre-release API surface.

## [1.0.5] - 2026-05-10

### Added
- `stability-config.conf` — targeted Compose compiler stability config (replaces unsafe wildcard)
- `NetworkMethod.UNKNOWN` — safe fallback for custom/unknown HTTP verbs; `fromString()` no longer silently maps to `GET`
- `androidMain` `callerTag()` — added Android-specific skip prefixes (`dalvik.`, `android.`) to stack walker
- **Network UI:** Single-scroll detail view — replaced three-tab layout (Overview/Request/Response) with a unified scrollable view; all sections visible without switching tabs
- **Network UI:** `StatusBadge` now shows actual HTTP code (e.g. `403`) instead of `ERR` for HTTP errors; `Error` label reserved for connection failures with no status code
- **Network UI:** Animated `Waiting for response…` indicator with pulsing dot shown in list row and detail view while a request is in-flight
- **Network UI:** Smart request section — distinguishes URL-only, query-parameter, body, and mixed request types; shows "No request body or parameters" for plain GET requests
- **Network UI:** Error section rendered in a selectable monospace red box after the request section; only appears for true connection failures (`UnknownHostException`, timeout), not HTTP 4xx/5xx
- **UI:** Timestamps are now displayed in a localized 12-hour AM/PM format (e.g. `06:52:10 PM`) instead of 24-hour military time
- **Interceptors:** `excludeHeaders` replaces `redactHeaders` — excluded headers are now completely removed from the UI (not shown as `***`); `OkHttpInterceptor.DEFAULT_EXCLUDED` and `AELogKtorInterceptor.DEFAULT_EXCLUDED` include 20 noisy system/auto-injected headers by default
- **Ktor interceptor:** Intercepts `HttpRequestPipeline.Render` to capture serialized request bodies (fixes missing POST bodies for data classes)
- **Copy text:** `toClipboardText()` now pretty-prints JSON bodies and matches the on-screen format exactly (method + URL, status, duration, request body, response body, error)

### Fixed
- **UI:** Bottom sheet now persists the currently selected tab across dismiss/reopen cycles
- **OkHttp:** Request bodies with `null` Content-Type (common with Retrofit converters) are now successfully captured as text instead of logged as `<binary or unsupported>`
- **Critical:** `LogEntry` default parameter side-effects — `id` and `timestamp` no longer have defaults; `copy()` no longer silently generates a new ID and timestamp
- **Critical:** `RingBuffer.replace()` off-by-one — bound check was `0..size` (inclusive), now correctly `0 until size`
- **Critical:** `LogConfig.enabled` was dead code — now correctly gates logging in `LogRecorder` alongside `AELog.isEnabled`
- **Critical:** `ExperimentalTime` API no longer leaks into consumer API — suppressed at module level via `compilerOptions.freeCompilerArgs` in all 4 affected modules; `@file:OptIn` annotations removed from source files
- **High:** `LogInspector.record()` now fans out to **all** `LogRecordSink` plugins, not just the first
- **High:** Ktor interceptor response body read wrapped in `runCatching` — pipeline no longer breaks on read failure; error is recorded instead
- **High:** `AnalyticsEvent` changed from `@Immutable` to `@Stable` — `Map<String, Any>` cannot guarantee immutability; `properties` type changed to `Map<String, String>`
- **Medium:** `AnalyticsTracker` uses proper imports instead of fully-qualified names throughout
- **Medium:** `AELog.configure()` — `LogInspector` constructed before CAS is now immediately GC-eligible on CAS loss (no live scopes)
- **Medium:** `NetworkMethod.valueOf()` crash on unknown HTTP verbs (CONNECT, TRACE, custom) in `NetworkRecorder` and `KtorInterceptor` — replaced with safe `fromString()`
- **Medium:** Race condition in `PluginManager.install()` — replaced TOCTOU check with `SynchronizedObject` lock ensuring `onAttach()` is never called twice
- **Network:** `NetworkEntry.statusLabel` prioritises HTTP status code over error flag — a 403 with a Ktor exception now shows `403`, not `ERR`
- **Network:** Ktor 4xx/5xx `ClientRequestException` / `ServerResponseException` no longer recorded as `error` — status code already captures the failure; `error` field is reserved for connection-level failures only
- **Network:** Ktor error messages strip the embedded `. Text: "..."` suffix that duplicated the response body
- `LogController.toggle()` non-atomic read-write — replaced with `MutableStateFlow.update { !it }`
- `LogConfig` KDoc referenced non-existent `AELog.create()` — corrected to `AELog.configure()`
- `AnalyticsTracker` KDoc referenced non-existent `AELog.default` — corrected to `AELog.analytics` proxy
- README: version references updated from `1.0.0` to `1.0.2`
- README: `AELog.v()` direct calls corrected to `AELog.log.v()` to match actual API
- README: Modularity callout referenced wrong artifact names (`logs-network`) — corrected to `log-network`

### Changed
- `Lifecycle` class is now `internal` — it was unreachable by consumers but incorrectly exposed in binary API
- `RingBuffer` class is now `internal` — it is a pure implementation detail of `PluginStorage`
- `AnalyticsTracker.track()` and `AnalyticsProxy.logEvent/logScreen` now accept `Map<String, String>` (was `Map<String, Any>`)
- `@kotlin.concurrent.Volatile` replaces `@Volatile` in `LogPlugin`, `NetworkPlugin`, `AnalyticsPlugin` for common multiplatform compatibility
- All modules: `explicitApiWarning()` → `explicitApi()` to enforce strict visibility for published library
- `log-network-ktor`: `ktor-client-core` changed from `implementation` to `api` — consumers need Ktor types transitively
- `log-network-okhttp`: `okhttp` changed from `implementation` to `api` — consumers need OkHttp types transitively
- `stability-config.conf` wildcard `com.ae.log.*` replaced with targeted entries for open filter classes only
- `OkHttpInterceptor`: `redactHeaders` → `excludeHeaders`; headers are filtered out entirely, not redacted with `***`
- `kotlin.native.ignoreDisabledTargets=true` added to `gradle.properties` to suppress iOS disabled-target warnings on Linux CI


## [1.0.4] - 2026-05-07

### Added
- **Modular Subprojects:** Completely reorganized and split the AELog project into independent sub-modules (e.g., `:core`, `:log-logs`, `:log-network`, `:log-network-ktor`, `:log-network-okhttp`, `:log-analytics`, `:log-crashes`).
- **Comprehensive Testing Suite:** Added full test coverage for Ktor and OkHttp interceptors alongside automated stress and performance benchmark suites.

### Changed
- **Standardized UI Naming:** Renamed all UI components to standardized `Log*` prefixes (like `LogNotchButton`, `LogInspectorPanel`).
- **AELog Interceptor Prefixes:** Relocated and standardized interceptor naming conventions using `AELog` prefixes (e.g., `AELogKtorInterceptor`, `AELogOkHttpInterceptor`).

## [1.0.3] - 2026-05-05

### Added
- **Pulsing Animation:** Added a dynamic, pulsing status dot indicator for in-flight requests in the Network inspector list and detail view.
- **Interactive UI Components:** Added reusable `ActionButton` and `ActionCard` layout components for plugin builders.

### Changed
- **Decoupled Architecture:** Extracted core functionality to simplify plugin registration and decouple core engine from plugin lifecycles.
- **Improved Security:** Standardized header exclude management in network interceptors.

### Fixed
- **API Robustness:** Enhanced thread safety and memory leak prevention across the core event bus and database storage layers.


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
- Thread-safe `LogStorage` with configurable max entries
- Plugin lifecycle: `onAttach → onOpen ⇄ onClose → onDetach`

[Unreleased]: https://github.com/abdo-essam/AELog/compare/v1.1.5...HEAD
[1.1.5]: https://github.com/abdo-essam/AELog/compare/v1.1.4...v1.1.5
[1.1.4]: https://github.com/abdo-essam/AELog/compare/v1.1.3...v1.1.4
[1.1.3]: https://github.com/abdo-essam/AELog/compare/v1.1.2...v1.1.3
[1.1.2]: https://github.com/abdo-essam/AELog/compare/v1.1.1...v1.1.2
[1.1.1]: https://github.com/abdo-essam/AELog/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/abdo-essam/AELog/compare/v1.0.9...v1.1.0
[1.0.9]: https://github.com/abdo-essam/AELog/compare/v1.0.8...v1.0.9
[1.0.8]: https://github.com/abdo-essam/AELog/compare/v1.0.7...v1.0.8
[1.0.7]: https://github.com/abdo-essam/AELog/compare/v1.0.6...v1.0.7
[1.0.6]: https://github.com/abdo-essam/AELog/compare/v1.0.5...v1.0.6
[1.0.5]: https://github.com/abdo-essam/AELog/compare/v1.0.4...v1.0.5
[1.0.4]: https://github.com/abdo-essam/AELog/compare/v1.0.3...v1.0.4
[1.0.3]: https://github.com/abdo-essam/AELog/compare/v1.0.2...v1.0.3
[1.0.2]: https://github.com/abdo-essam/AELog/compare/v1.0.1...v1.0.2
[1.0.1]: https://github.com/abdo-essam/AELog/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/abdo-essam/AELog/releases/tag/v1.0.0
