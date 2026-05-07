# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Real dark color scheme (`DarkColorScheme`) in `LogTheme` — automatically follows system dark/light mode
- `stability-config.conf` — Compose compiler stability config for better recomposition performance

### Fixed
- **Critical:** `NetworkMethod.valueOf()` crash on unknown HTTP verbs (CONNECT, TRACE, custom) in `NetworkRecorder` and `KtorInterceptor` — replaced with safe `fromString()`
- **Critical:** Race condition in `PluginManager.install()` — replaced TOCTOU check with `SynchronizedObject` lock ensuring `onAttach()` is never called twice
- `LogController.toggle()` non-atomic read-write — replaced with `MutableStateFlow.update { !it }`
- `LogConfig` KDoc referenced non-existent `AELog.create()` — corrected to `AELog.init()`
- `AnalyticsTracker` KDoc referenced non-existent `AELog.default` — corrected to `AELog.analytics` proxy
- Missing `kotlinx.coroutines.flow.update` import in `LogController`
- README: version references updated from `1.0.0` to `1.0.2`
- README: `AELog.v()` direct calls corrected to `AELog.log.v()` to match actual API
- README: Modularity callout referenced wrong artifact names (`logs-network`) — corrected to `log-network`

### Changed
- All modules: `explicitApiWarning()` → `explicitApi()` to enforce strict visibility for published library
- `log-network-ktor`: `ktor-client-core` changed from `implementation` to `api` — consumers need Ktor types transitively
- `log-network-okhttp`: `okhttp` changed from `implementation` to `api` — consumers need OkHttp types transitively
- Ktor version unified to `3.4.3` across all Ktor dependencies (was mismatched `3.0.0` + `3.4.3`)

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
