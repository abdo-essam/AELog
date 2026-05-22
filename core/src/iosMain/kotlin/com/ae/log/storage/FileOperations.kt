package com.ae.log.storage

import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

internal actual class FileOperations actual constructor(
    private val directoryPath: String,
) {
    private val fileManager = NSFileManager.defaultManager

    actual fun ensureDirectoryExists() {
        if (!fileManager.fileExistsAtPath(directoryPath)) {
            fileManager.createDirectoryAtPath(
                directoryPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
    }

    actual fun writeFile(content: String) {
        val fileName = "${currentTimeMillis()}_${counter++}.json"
        @Suppress("CAST_NEVER_SUCCEEDS")
        val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
        (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    }

    actual fun readAllFiles(): List<String> {
        val contents = fileManager.contentsOfDirectoryAtPath(directoryPath, error = null)
            ?: return emptyList()

        @Suppress("UNCHECKED_CAST")
        val files = (contents as List<String>)
            .filter { it.endsWith(".json") }
            .sorted()

        return files.mapNotNull { fileName ->
            @Suppress("CAST_NEVER_SUCCEEDS")
            val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
            NSString.create(contentsOfFile = filePath, encoding = NSUTF8StringEncoding, error = null) as? String
        }
    }

    actual fun deleteAllFiles() {
        val contents = fileManager.contentsOfDirectoryAtPath(directoryPath, error = null)
            ?: return

        @Suppress("UNCHECKED_CAST")
        (contents as List<String>)
            .filter { it.endsWith(".json") }
            .forEach { fileName ->
                @Suppress("CAST_NEVER_SUCCEEDS")
                val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
                fileManager.removeItemAtPath(filePath, error = null)
            }
    }

    private fun currentTimeMillis(): Long =
        (platform.Foundation.NSDate().timeIntervalSince1970 * 1000).toLong()

    private companion object {
        var counter = 0
    }
}
