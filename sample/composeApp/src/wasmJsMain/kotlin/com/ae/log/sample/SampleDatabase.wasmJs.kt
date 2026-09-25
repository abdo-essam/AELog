package com.ae.log.sample

import com.ae.log.AELog
import com.ae.log.database.database

public actual fun ensureSampleDatabaseExists(): String {
    AELog.database.registerDatabase("web_sample.db", "web_sample.db")
    AELog.database.logQuery("web_sample.db", "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, role TEXT);", 2L, "users")
    AELog.database.logQuery("web_sample.db", "SELECT * FROM users WHERE active = 1;", 4L, "users")
    AELog.database.logQuery("web_sample.db", "INSERT INTO users (name, role) VALUES ('Web Developer', 'Admin');", 3L, "users", affectedRows = 1L)
    return "Sample database registered for Web"
}

public actual fun insertSampleUser(
    name: String,
    email: String,
    role: String,
): Boolean {
    AELog.database.logQuery("web_sample.db", "INSERT INTO users (name, role) VALUES ('$name', '$role');", 4L, "users", affectedRows = 1L)
    return true
}

public actual fun deleteSampleDatabase(): Boolean {
    AELog.database.logQuery("web_sample.db", "DROP DATABASE web_sample.db;", 5L, isSuccess = true)
    return true
}
