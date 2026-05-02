# Contributing to AELog

First off, thank you for considering contributing! 🎉

## Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Architecture Overview](#architecture-overview)

## Code of Conduct

This project follows our [Code of Conduct](CODE_OF_CONDUCT.md). 
By participating, you agree to uphold this code.

## Getting Started

### Prerequisites
- JDK 17+
- Android Studio Ladybug or later / Fleet
- Xcode 15+ (for iOS targets)
- Kotlin 2.3.0+

### Development Setup

```bash
# Clone the repository
git clone https://github.com/abdo-essam/AELog.git
cd AELog

# Build the project
./gradlew build

# Run tests
./gradlew allTests

# Run the sample app (Android)
./gradlew :sample:composeApp:installDebug

# Check code formatting
./gradlew spotlessCheck

# Fix code formatting
./gradlew spotlessApply

# Generate API docs
./gradlew dokkaGeneratePublicationHtml

# Verify binary compatibility
./gradlew apiCheck
```

## How to Contribute
### 🐛 Reporting Bugs
* Use the Bug Report template
* Include: KMP version, platform, steps to reproduce, expected vs actual behavior

### 💡 Suggesting Features
* Use the Feature Request template
* Describe the use case, not just the solution

### 🔧 Submitting Changes
* Fork the repository
* Create a feature branch: `git checkout -b feat/my-feature`
* Make your changes
* Add/update tests
* Run the full check: `./gradlew check`
* Commit using Conventional Commits
* Push and open a Pull Request

## Pull Request Process
* Fill out the PR template completely
* Ensure CI passes — all checks must be green
* One approval required from a maintainer
* Squash merge is preferred for clean history
* Update CHANGELOG.md under `[Unreleased]`

## Coding Standards
### Kotlin Style
* Follow Kotlin Coding Conventions
* Use `explicitApi()` — all public APIs need visibility modifiers
* Mark internal APIs with `internal` keyword
* KDoc on all public classes, functions, and properties

### Commit Conventions
We use Conventional Commits:

```text
feat: add network inspection plugin
fix: resolve theme bleed into host app
docs: update plugin creation guide
test: add LogStore unit tests
refactor: extract log filtering to separate class
chore: update Kotlin to 2.3.0
perf: cache LogEntry computed properties
ci: add iOS build to CI pipeline
```

### Architecture Rules
* Plugins must implement `UIPlugin` or `DataPlugin`
* No Android-specific imports in `commonMain`
* All state exposed as `StateFlow` (no LiveData)
* Compose UI uses Material3 exclusively
* Keep the public API surface minimal

## Architecture Overview
```text
┌─────────────────────────────────────────┐
│              AELogProvider           │  ← Compose entry point
├─────────────────────────────────────────┤
│              AELog                  │  ← Core engine
├──────────┬──────────┬───────────────────┤
│ LogPlugin│ (Your   │  DataPlugin       │  ← Plugin system
│ (UIPlugin)│  Plugin) │  (headless)       │
├──────────┴──────────┴───────────────────┤
│              LogStore / DataStore        │  ← Storage layer
└─────────────────────────────────────────┘
```
