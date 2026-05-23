package com.ae.log.utils

import java.io.File

internal class AndroidFileOperations(
    private val directoryPath: String,
) : FileOperations {
    override fun ensureDirectoryExists() {
        val dir = File(directoryPath)
        if (!dir.exists()) dir.mkdirs()
    }

    override fun writeFile(content: String) {
        val dir = File(directoryPath)
        val fileName = "${System.currentTimeMillis()}_${counter++}.json"
        File(dir, fileName).writeText(content)
    }

    override fun readAllFiles(): List<String> {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir
            .listFiles()
            ?.filter { it.isFile && it.extension == "json" }
            ?.sortedBy { it.nameWithoutExtension }
            ?.map { it.readText() }
            ?: emptyList()
    }

    override fun deleteAllFiles() {
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

public actual fun createFileOperations(directoryPath: String): FileOperations =
    AndroidFileOperations(directoryPath)
