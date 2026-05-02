# Logging Integrations

AELog works with **any** logging library. Just forward logs to `AELog.default.log()`.

## Kermit

```kotlin
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

class AELogKermitWriter(
    private val inspector: AELog = AELog.default
) : LogWriter() {
    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        inspector.log(
            severity = severity.toAELogLogSeverity(),
            tag = tag,
            message = buildString {
                append(message)
                throwable?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}

private fun Severity.toAELogLogSeverity(): LogSeverity = when (this) {
    Severity.Verbose -> LogSeverity.VERBOSE
    Severity.Debug -> LogSeverity.DEBUG
    Severity.Info -> LogSeverity.INFO
    Severity.Warn -> LogSeverity.WARN
    Severity.Error -> LogSeverity.ERROR
    Severity.Assert -> LogSeverity.ASSERT
}

// Setup
Logger.addLogWriter(AELogKermitWriter())
```

## Napier

```kotlin
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

class AELogNapierAntilog(
    private val inspector: AELog = AELog.default
) : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        inspector.log(
            severity = priority.toAELogLogSeverity(),
            tag = tag ?: "Napier",
            message = buildString {
                message?.let { append(it) }
                throwable?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}

private fun LogLevel.toAELogLogSeverity(): LogSeverity = when (this) {
    LogLevel.VERBOSE -> LogSeverity.VERBOSE
    LogLevel.DEBUG -> LogSeverity.DEBUG
    LogLevel.INFO -> LogSeverity.INFO
    LogLevel.WARNING -> LogSeverity.WARN
    LogLevel.ERROR -> LogSeverity.ERROR
    LogLevel.ASSERT -> LogSeverity.ASSERT
}

// Setup
Napier.base(AELogNapierAntilog())
```

## Timber (Android)

```kotlin
import timber.log.Timber
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

class AELogTimberTree(
    private val inspector: AELog = AELog.default
) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        inspector.log(
            severity = priority.toAELogLogSeverity(),
            tag = tag ?: "Timber",
            message = buildString {
                append(message)
                t?.let { append("\n${it.stackTraceToString()}") }
            }
        )
    }
}

private fun Int.toAELogLogSeverity(): LogSeverity = when (this) {
    android.util.Log.VERBOSE -> LogSeverity.VERBOSE
    android.util.Log.DEBUG -> LogSeverity.DEBUG
    android.util.Log.INFO -> LogSeverity.INFO
    android.util.Log.WARN -> LogSeverity.WARN
    android.util.Log.ERROR -> LogSeverity.ERROR
    android.util.Log.ASSERT -> LogSeverity.ASSERT
    else -> LogSeverity.DEBUG
}

// Setup
Timber.plant(AELogTimberTree())
```

## KotlinLogging / SLF4J

```kotlin
import org.slf4j.event.Level
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

class AELogSlf4jAppender(
    private val inspector: AELog = AELog.default
) : AppenderBase<ILoggingEvent>() {
    override fun append(event: ILoggingEvent) {
        inspector.log(
            severity = event.level.toAELogLogSeverity(),
            tag = event.loggerName.substringAfterLast('.'),
            message = event.formattedMessage
        )
    }
}

private fun Level.toAELogLogSeverity(): LogSeverity = when (this) {
    Level.TRACE -> LogSeverity.VERBOSE
    Level.DEBUG -> LogSeverity.DEBUG
    Level.INFO -> LogSeverity.INFO
    Level.WARN -> LogSeverity.WARN
    Level.ERROR -> LogSeverity.ERROR
}
```

## Ktor Client Logging

```kotlin
import io.ktor.client.*
import io.ktor.client.plugins.logging.*
import com.ae.log.AELog
import com.ae.log.plugins.log.model.LogSeverity

val client = HttpClient {
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                AELog.default.log(
                    severity = LogSeverity.DEBUG,
                    tag = "HTTP",
                    message = message
                )
            }
        }
        level = LogLevel.ALL
    }
}
```

## Direct Usage (No Library)

```kotlin
// Just call log() directly — no bridge needed
val inspector = AELog.default

inspector.log(LogSeverity.INFO, "MyApp", "App started")
inspector.log(LogSeverity.ERROR, "Auth", "Login failed: $error")
```
