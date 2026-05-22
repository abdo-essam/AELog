package com.ae.log.storage

import java.io.File

internal actual class FileOperations actual constructor(
    private val directoryPath: String,
) {
    actual fun ensureDirectoryExists() {
        val dir = File(directoryPath)
        if (!dir.exists()) dir.mkdirs()
    }

    actual fun writeFile(content: String) {
        val dir = File(directoryPath)
        val fileName = "${System.currentTimeMillis()}_${counter++}.json"
        File(dir, fileName).writeText(content)
    }

    actual fun readAllFiles(): List<String> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir
            .listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.sortedBy { it.nameWithoutExtension }
            ?.map { it.readText() }
            ?: emptyList()
    }

    actual fun deleteAllFiles() {
        val dir = File(directoryPath)
        dir
            .listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.forEach { it.delete() }
    }

    private companion object {
        var counter = 0
    }
}
