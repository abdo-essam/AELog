package com.ae.log.database.inspector

import com.ae.log.database.config.DatabasePluginConfig

/**
 * Creates the platform-specific default [DatabaseInspector].
 */
internal expect fun createPlatformDatabaseInspector(config: DatabasePluginConfig): DatabaseInspector
