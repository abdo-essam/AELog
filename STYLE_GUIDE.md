# AELog Naming Style Guide

Naming is one of the most critical aspects of maintaining a healthy, readable, and predictable codebase. This style guide defines the conventions for the `AELog` library to ensure consistency across all modules and plugins.

## 1. Prefix Policy

**No Hungarian Notation or Framework Prefixes.**
- **Do not** use the `AE` prefix (or any other library-specific prefix) on classes, interfaces, composables, or objects.
- Use Kotlin's package mechanism for namespaces. If consumers experience naming collisions, they can use import aliases (`import com.ae.log.Logger as AELogger`).
- *Example:* Use `LogContainer`, `Theme`, `Spacing`, `PanelHeader` instead of `LogContainer`, `LogTheme`, etc.

## 2. Event Tense

**Events describe things that have already happened.**
- Always use **past tense** for event names.
- Do not use imperative commands pretending to be events. Commands should be direct method calls, not dispatched events.
- *Good:* `AppStartedEvent`, `PanelOpenedEvent`, `LogTagRegisteredEvent`, `AllDataClearedEvent`
- *Bad:* `RegisterLogTagEvent` (imperative), `OpenPanelEvent` (imperative)

## 3. Suffix Conventions

Be intentional and consistent with suffixes to immediately communicate what a type represents.

- **`*Entry` vs `*Event`**: 
  - Use `*Entry` for records or items stored in the ring buffer/store (e.g., `LogEntry`, `NetworkEntry`, `AnalyticsEntry`).
  - Use `*Event` only for the Event Bus system. Do not use `Event` to describe data records.
- **`*Filter`**:
  - Do not use a generic `*Filter` suffix unless it accurately describes the concept. Be specific about *what* is being filtered.
  - *Good:* `LogSeverityFilter`, `NetworkStatusFilter`, `AnalyticsCategoryFilter`
  - *Bad:* `LogFilter` (when it only filters severity), `NetworkFilter`

## 4. Verb Usage

Ensure verbs accurately reflect the action and origin.

- **Lifecycle and Callbacks**:
  - Use `on*` for standard callbacks (e.g., `onStart`, `onStop`).
  - When dispatching lifecycle events to plugins or systems, use explicit dispatch verbs like `onAppStart()`, `dispatchAppStart()`, or `notifyAppStart()`.
  - Ensure symmetry (e.g., `onAppStart()` and `onAppStop()`). Do not mix prefixes like `notifyStart()` and `clearAll()`.
- **Descriptive Actions**:
  - Avoid vague words like `clean`. Be specific about what the action does.
  - *Good:* `messageWithoutHttpFrames`, `bodyOnly`
  - *Bad:* `cleanMessage`
  - Ensure verbs are used as actions, and nouns are used as things.
  - *Good:* `trackScreen("Home")`, `viewedScreen("Home")`
  - *Bad:* `screen("Home")`

## 5. Avoiding Redundancy

Do not repeat words unnecessarily in class or method names. If the class name provides context, the method name doesn't need to repeat it.

- *Good:* `IdGenerator.next()`, `Ids.generate()`
- *Bad:* `IdGenerator.generateId()`

## 6. Indirection and Aliasing

Avoid creating deep wrapper classes that add no semantic value.

- If a class simply wraps another with exact parity, use a `typealias` instead of creating a new type (e.g., `typealias LogStorage = PluginStorage<LogEntry>`). This flattens the hierarchy and reduces cognitive load.
