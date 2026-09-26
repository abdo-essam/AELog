package com.ae.log.sample

public expect fun ensureSampleDatabaseExists(): String

public expect fun insertSampleUser(
    name: String,
    email: String,
    role: String,
): Boolean

public expect fun deleteSampleDatabase(): Boolean
