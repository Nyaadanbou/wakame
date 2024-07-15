package random2

import net.kyori.adventure.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeContainerTest {

    @Test
    fun `should resolve local node values correctly`() {
        val sharedStorage = SharedStorage<String>()
        val nodeContainer = NodeContainer(sharedStorage)

        val rootNode = CompositeNode<String>(Key.key("root")).apply {
            addNode {
                node("foo:1", "Value 1")
                node("foo:2", "Value 2")
            }
        }

        nodeContainer.root = rootNode

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2"), values)
    }

    @Test
    fun `should resolve global node values correctly`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("bar1") {
                node("foo:3", "Global Value 3")
            }
            addEntry("bar2") {
                node("foo:4", "Global Value 4")
                node("foo:5", "Global Value 5")
            }
            addEntry("bar3") {
                compositeNode("global:bar2")
                node("foo:6", "Global Value 6")
                node("foo:7", "Global Value 7")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)

        val rootNode = CompositeNode<String>(Key.key("root")).apply {
            addNode { node("foo:1", "Value 1") }
            addNode { node("foo:2", "Value 2") }
            addNode { compositeNode("global:bar1") }
            addNode { compositeNode("global:bar3") }
        }

        nodeContainer.root = rootNode

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5", "Global Value 6", "Global Value 7"), values)
    }

    @Test
    fun `should resolve mixed node values correctly`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("bar1") {
                node("foo:3", "Global Value 3")
            }
            addEntry("bar2") {
                node("foo:4", "Global Value 4")
                node("foo:5", "Global Value 5")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)

        val rootNode = CompositeNode<String>(Key.key("root")).apply {
            addNode { node("foo:1", "Value 1") }
            addNode { node("foo:2", "Value 2") }
            addNode { compositeNode("global:bar1") }
            addNode { compositeNode("global:bar2") }
        }

        nodeContainer.root = rootNode

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
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("nested1") {
                node("foo:nested1", "Nested Value 1")
            }
        }
        val nodeContainer = NodeContainer(sharedStorage)

        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:nested1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Nested Value 1"), values)
    }

    @Test
    fun `should correctly resolve multiple layers of nested nodes`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("nested1") {
                compositeNode("global:nested2")
            }
            addEntry("nested2") {
                node("foo:nested2", "Nested Value 2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:nested1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Nested Value 2"), values)
    }

    @Test
    fun `should handle empty global data source without errors`() {
        val sharedStorage = SharedStorage<String>()
        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root"))

        val values = nodeContainer.values()
        assertTrue(values.isEmpty())
    }

    @Test
    fun `should include duplicate values from nested nodes with DSL`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("nested") {
                node("foo:nested", "Duplicate Value")
                node("foo:nested", "Duplicate Value")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:nested") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Duplicate Value", "Duplicate Value"), values)
    }

    @Test
    fun `should correctly resolve deeply nested nodes across multiple global keys`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("nested1") {
                compositeNode("global:nested2")
            }
            addEntry("nested2") {
                compositeNode("global:nested3")
            }
            addEntry("nested3") {
                node("foo:nested3", "Deeply Nested Value 3")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:nested1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Deeply Nested Value 3"), values)
    }

    @Test
    fun `should correctly resolve single layer of nested composite nodes`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("layer1") {
                node("foo:layer1", "Layer 1 Value")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:layer1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 1 Value"), values)
    }

    @Test
    fun `should correctly resolve two layers of nested composite nodes`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("layer2") {
                node("foo:layer2", "Layer 2 Value")
            }
            addEntry("layer1") {
                compositeNode("global:layer2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:layer1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 2 Value"), values)
    }

    @Test
    fun `should correctly resolve three layers of nested composite nodes`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("layer3") {
                node("foo:layer3", "Layer 3 Value")
            }
            addEntry("layer2") {
                compositeNode("global:layer3")
            }
            addEntry("layer1") {
                compositeNode("global:layer2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:layer1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 3 Value"), values)
    }

    @Test
    fun `should correctly resolve four layers of nested composite nodes`() {
        val sharedStorage = SharedStorage<String>().apply {
            addEntry("layer4") {
                node("foo:layer4", "Layer 4 Value")
            }
            addEntry("layer3") {
                compositeNode("global:layer4")
            }
            addEntry("layer2") {
                compositeNode("global:layer3")
            }
            addEntry("layer1") {
                compositeNode("global:layer2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage)
        nodeContainer.root = CompositeNode<String>(Key.key("root")).apply {
            addNode { compositeNode("global:layer1") }
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 4 Value"), values)
    }

}