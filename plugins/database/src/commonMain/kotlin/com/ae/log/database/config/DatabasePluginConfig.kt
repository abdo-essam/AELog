package com.ae.log.database.config

/**
 * Configuration options for the AELog Database Plugin.
 *
 * @property allowWrite If `true`, enables write operations (INSERT, UPDATE, DELETE, DROP, CREATE)
 * in the interactive SQL console with user confirmation. Defaults to `false` (safe read-only mode).
 * @property busyTimeoutMs SQLite `busy_timeout` in milliseconds to wait before failing on concurrent locks in WAL mode.
 * @property defaultPageSize Default number of rows loaded per page when browsing table data.
 * @property passphraseProvider Provider callback for unlocking encrypted databases (e.g. SQLCipher).
 * @property additionalSearchPaths Additional directory paths to scan when searching for SQLite database files.
 */
public data class DatabasePluginConfig(
    public val allowWrite: Boolean = false,
    public val busyTimeoutMs: Long = 3000L,
    public val defaultPageSize: Int = 50,
    public val passphraseProvider: PassphraseProvider? = null,
    public val additionalSearchPaths: List<String> = emptyList(),
)
