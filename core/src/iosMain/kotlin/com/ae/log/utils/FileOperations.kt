@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ae.log.utils

import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile

@OptIn(kotlinx.cinterop.BetaInteropApi::class)
internal class IosFileOperations(
    private val directoryPath: String,
) : FileOperations {
    private val fileManager = NSFileManager.defaultManager

    override fun ensureDirectoryExists() {
        if (!fileManager.fileExistsAtPath(directoryPath)) {
            fileManager.createDirectoryAtPath(
                directoryPath,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
    }

    override fun writeFile(content: String) {
        val fileName = "${currentTimeMillis()}_${counter++}.json"

        @Suppress("CAST_NEVER_SUCCEEDS")
        val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
        (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    }

    override fun readAllFiles(): List<String> {
        @Suppress("UNCHECKED_CAST")
        val files =
            (fileManager.contentsOfDirectoryAtPath(directoryPath, error = null) as? List<String>)
                ?.filter { it.endsWith(".json") }
                ?.sorted()
                ?: emptyList()

        return files.mapNotNull { fileName ->
            @Suppress("CAST_NEVER_SUCCEEDS")
            val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
            NSString.create(contentsOfFile = filePath, encoding = NSUTF8StringEncoding, error = null) as? String
        }
    }

    override fun deleteAllFiles() {
        @Suppress("UNCHECKED_CAST")
        val files =
            (fileManager.contentsOfDirectoryAtPath(directoryPath, error = null) as? List<String>)
                ?.filter { it.endsWith(".json") }
                ?: return

        files.forEach { fileName ->
            @Suppress("CAST_NEVER_SUCCEEDS")
            val filePath = (directoryPath as NSString).stringByAppendingPathComponent(fileName)
            fileManager.removeItemAtPath(filePath, error = null)
        }
    }

    private fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

    private companion object {
        var counter = 0
    }
}

public actual fun createFileOperations(directoryPath: String): FileOperations = IosFileOperations(directoryPath)
