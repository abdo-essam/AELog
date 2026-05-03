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
