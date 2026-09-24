package com.ae.log.sample

import com.ae.log.AELog
import com.ae.log.database.database

public actual fun ensureSampleDatabaseExists(): String {
    AELog.database.registerDatabase("web_sample.db", "web_sample.db")
    return "Sample database registered for Web"
}

public actual fun insertSampleUser(
    name: String,
    email: String,
    role: String,
): Boolean = true

public actual fun deleteSampleDatabase(): Boolean = true
