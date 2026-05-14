package com.ae.log.storage

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginStorageTest {
    private lateinit var storage: PluginStorage<String>

    @BeforeTest
    fun setUp() {
        storage = PluginStorage(capacity = 3)
    }

    @Test
    fun `add - emits new list via dataFlow`() =
        runTest {
            storage.add("a")
            storage.add("b")
            assertEquals(listOf("a", "b"), storage.dataFlow.value)
        }

    @Test
    fun `add - evicts oldest when over capacity`() =
        runTest {
            storage.add("a")
            storage.add("b")
            storage.add("c")
            storage.add("d") // evicts "a"
            assertEquals(listOf("b", "c", "d"), storage.dataFlow.value)
        }

    @Test
    fun `clear - resets dataFlow to empty`() =
        runTest {
            storage.add("a")
            storage.add("b")
            storage.clear()
            assertTrue(storage.dataFlow.value.isEmpty())
        }

    @Test
    fun `addOrReplace - replaces matching element by predicate`() =
        runTest {
            storage.add("apple")
            storage.add("banana")
            storage.addOrReplace(predicate = { it == "banana" }, item = "blueberry")
            assertEquals(listOf("apple", "blueberry"), storage.dataFlow.value)
        }

    @Test
    fun `addOrReplace - adds new element when predicate does not match`() =
        runTest {
            storage.add("apple")
            storage.addOrReplace(predicate = { it == "mango" }, item = "banana")
            assertEquals(listOf("apple", "banana"), storage.dataFlow.value)
        }

    @Test
    fun `updateFirst - transforms matching element`() =
        runTest {
            storage.add("hello")
            storage.add("world")
            storage.updateFirst(predicate = { it == "world" }, transform = { it.uppercase() })
            assertEquals(listOf("hello", "WORLD"), storage.dataFlow.value)
        }

    @Test
    fun `updateFirst - is no-op when no element matches`() =
        runTest {
            storage.add("hello")
            storage.updateFirst(predicate = { it == "nothing" }, transform = { "replaced" })
            assertEquals(listOf("hello"), storage.dataFlow.value)
        }

    @Test
    fun `dataFlow - initial value is empty list`() {
        assertTrue(storage.dataFlow.value.isEmpty())
    }
}
