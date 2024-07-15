package random2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeContainerTest {

    @Test
    fun `should resolve local node values correctly`() {
        val sharedStorage = SharedStorage<String>()
        val nodeContainer = NodeContainer(sharedStorage) {
            local("foo:1", "Value 1")
            local("foo:2", "Value 2")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2"), values)
    }

    @Test
    fun `should resolve global node values correctly`() {
        val sharedStorage = SharedStorage {
            addEntry("bar1") {
                local("foo:3", "Global Value 3")
            }
            addEntry("bar2") {
                local("foo:4", "Global Value 4")
                local("foo:5", "Global Value 5")
            }
            addEntry("bar3") {
                composite("global:bar2")
                local("foo:6", "Global Value 6")
                local("foo:7", "Global Value 7")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            local("foo:1", "Value 1")
            local("foo:2", "Value 2")
            composite("global:bar1")
            composite("global:bar3")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5", "Global Value 6", "Global Value 7"), values)
    }

    @Test
    fun `should resolve mixed node values correctly`() {
        val sharedStorage = SharedStorage {
            addEntry("bar1") {
                local("foo:3", "Global Value 3")
            }
            addEntry("bar2") {
                local("foo:4", "Global Value 4")
                local("foo:5", "Global Value 5")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            local("foo:1", "Value 1")
            local("foo:2", "Value 2")
            composite("global:bar1")
            composite("global:bar2")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5"), values)
    }

    @Test
    fun `should handle empty container without errors`() {
        val sharedStorage = SharedStorage<String>()
        val nodeContainer = NodeContainer(sharedStorage)

        val values = nodeContainer.values()
        assertTrue(values.isEmpty())
    }

    @Test
    fun `should correctly resolve single layer of nested nodes`() {
        val sharedStorage = SharedStorage {
            addEntry("nested1") {
                local("foo:nested1", "Nested Value 1")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:nested1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Nested Value 1"), values)
    }

    @Test
    fun `should correctly resolve multiple layers of nested nodes`() {
        val sharedStorage = SharedStorage {
            addEntry("nested1") {
                composite("global:nested2")
            }
            addEntry("nested2") {
                local("foo:nested2", "Nested Value 2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:nested1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Nested Value 2"), values)
    }

    @Test
    fun `should handle empty global data source without errors`() {
        val sharedStorage = SharedStorage<String>()
        val nodeContainer = NodeContainer(sharedStorage) { /* empty */ }

        val values = nodeContainer.values()
        assertTrue(values.isEmpty())
    }

    @Test
    fun `should include duplicate values from nested nodes with DSL`() {
        val sharedStorage = SharedStorage {
            addEntry("nested") {
                local("foo:nested", "Duplicate Value")
                local("foo:nested", "Duplicate Value")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:nested")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Duplicate Value", "Duplicate Value"), values)
    }

    @Test
    fun `should correctly resolve deeply nested nodes across multiple global keys`() {
        val sharedStorage = SharedStorage {
            addEntry("nested1") {
                composite("global:nested2")
            }
            addEntry("nested2") {
                composite("global:nested3")
            }
            addEntry("nested3") {
                local("foo:nested3", "Deeply Nested Value 3")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:nested1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Deeply Nested Value 3"), values)
    }

    @Test
    fun `should correctly resolve single layer of nested composite nodes`() {
        val sharedStorage = SharedStorage {
            addEntry("layer1") {
                local("foo:layer1", "Layer 1 Value")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:layer1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 1 Value"), values)
    }

    @Test
    fun `should correctly resolve two layers of nested composite nodes`() {
        val sharedStorage = SharedStorage {
            addEntry("layer2") {
                local("foo:layer2", "Layer 2 Value")
            }
            addEntry("layer1") {
                composite("global:layer2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:layer1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 2 Value"), values)
    }

    @Test
    fun `should correctly resolve three layers of nested composite nodes`() {
        val sharedStorage = SharedStorage {
            addEntry("layer3") {
                local("foo:layer3", "Layer 3 Value")
            }
            addEntry("layer2") {
                composite("global:layer3")
            }
            addEntry("layer1") {
                composite("global:layer2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:layer1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 3 Value"), values)
    }

    @Test
    fun `should correctly resolve four layers of nested composite nodes`() {
        val sharedStorage = SharedStorage {
            addEntry("layer4") {
                local("foo:layer4", "Layer 4 Value")
            }
            addEntry("layer3") {
                composite("global:layer4")
            }
            addEntry("layer2") {
                composite("global:layer3")
            }
            addEntry("layer1") {
                composite("global:layer2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite("global:layer1")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 4 Value"), values)
    }

}