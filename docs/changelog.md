# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- 

### Changed
- 

### Fixed
- 

## [1.0.0] - 2025-07-XX

### Added
- 🎉 Initial stable release `v1.0.0`
- Extracted features into specialized independent plugins (`:devlens-network`, `:devlens-analytics`)
- Built-in `LogsPlugin` with search, severity filtering, and copy features
- Material3 themed UI with light/dark mode support and `DevLensUiConfig` overrides
- Shared standard UI components (`DevLensViewerHeader`, `DevLensSearchBar`, `DevLensFilterChips`)
- Adaptive layout: bottom sheet (phones) / dialog (tablets)
- Configurable floating debug button and multi-finger long-press triggers
- Expanded Request/Response details view with JSON payloads in `NetworkPlugin`
- FlowRow property chips in `AnalyticsPlugin`
- Safe global static APIs (`DevLens.i`, `NetworkApi`, `AnalyticsApi`)

### Architecture
- fully reactive `Model-Store-API-UI` pattern
- Idempotent thread-safe plugin mapping via `DevLensSetup.init()`
- Replaced global mutable states with strictly managed `StateFlow` scopes
- Removed `reified` generic JVM restrictions for better multiplatform compatibility
- Switched default `Instant` logic to Standard library `Clock.System` for platform consistency

[Unreleased]: https://github.com/abdo-essam/AEDevLens/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/abdo-essam/AEDevLens/releases/tag/v1.0.0
