package com.ae.log.storage

import com.ae.log.utils.FileOperations
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersistentPluginStorageTest {

    private class MockFileOperations : FileOperations {
        var directoryCreated = false
        val files = mutableMapOf<String, String>()
        var counter = 0

        override fun ensureDirectoryExists() {
            directoryCreated = true
        }

        override fun writeFile(content: String) {
            files["file_${counter++}.json"] = content
        }

        override fun readAllFiles(): List<String> {
            return files.keys.sorted().mapNotNull { files[it] }
        }

        override fun deleteAllFiles() {
            files.clear()
        }
    }

    @Test
    fun testInitializationLoadsExistingData() {
        val mockFileOps = MockFileOperations()
        mockFileOps.writeFile("\"first\"")
        mockFileOps.writeFile("\"second\"")

        val storage = PersistentPluginStorage(
            directoryPath = "/test",
            serializer = String.serializer(),
            fileOps = mockFileOps
        )

        assertTrue(mockFileOps.directoryCreated)
        assertEquals(listOf("first", "second"), storage.dataFlow.value)
    }

    @Test
    fun testAddItemPersistsToDiskAndUpdatesState() {
        val mockFileOps = MockFileOperations()
        val storage = PersistentPluginStorage(
            directoryPath = "/test",
            serializer = String.serializer(),
            fileOps = mockFileOps
        )

        storage.add("hello")
        storage.add("world")

        assertEquals(listOf("hello", "world"), storage.dataFlow.value)
        assertEquals(2, mockFileOps.files.size)
        assertEquals(listOf("\"hello\"", "\"world\""), mockFileOps.readAllFiles())
    }

    @Test
    fun testClearRemovesAllData() {
        val mockFileOps = MockFileOperations()
        val storage = PersistentPluginStorage(
            directoryPath = "/test",
            serializer = String.serializer(),
            fileOps = mockFileOps
        )

        storage.add("one")
        storage.add("two")
        assertEquals(2, storage.dataFlow.value.size)

        storage.clear()
        assertTrue(storage.dataFlow.value.isEmpty())
        assertTrue(mockFileOps.files.isEmpty())
    }
}
