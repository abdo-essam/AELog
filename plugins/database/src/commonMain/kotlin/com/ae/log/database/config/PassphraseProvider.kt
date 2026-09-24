package com.ae.log.database.config

/**
 * Functional interface to supply passphrases for encrypted databases (e.g. SQLCipher).
 */
public fun interface PassphraseProvider {
    /**
     * Return the encryption passphrase for [dbName], or `null` if no passphrase is known.
     *
     * @param dbName Name of the database (e.g. `"encrypted_app.db"`).
     * @return The secret passphrase as a [CharArray], or `null` if unable to unlock.
     */
    public fun getPassphrase(dbName: String): CharArray?
}
