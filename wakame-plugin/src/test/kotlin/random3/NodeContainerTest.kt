package random3

import cc.mewcraft.wakame.random3.NodeContainer
import cc.mewcraft.wakame.random3.NodeRepository
import net.kyori.adventure.key.Key.key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeContainerTest {

    @Test
    fun `should resolve local node values correctly`() {
        val sharedStorage = NodeRepository<String>()
        val nodeContainer = NodeContainer(sharedStorage) {
            local(key("foo:1"), "Value 1")
            local(key("foo:2"), "Value 2")
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2"), values)
    }

    @Test
    fun `should resolve global node values correctly`() {
        val sharedStorage = NodeRepository {
            addEntry("bar1") {
                local(key("foo:3"), "Global Value 3")
            }
            addEntry("bar2") {
                local(key("foo:4"), "Global Value 4")
                local(key("foo:5"), "Global Value 5")
            }
            addEntry("bar3") {
                composite(key("global:bar2"))
                local(key("foo:6"), "Global Value 6")
                local(key("foo:7"), "Global Value 7")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            local(key("foo:1"), "Value 1")
            local(key("foo:2"), "Value 2")
            composite(key("global:bar1"))
            composite(key("global:bar3"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5", "Global Value 6", "Global Value 7"), values)
    }

    @Test
    fun `should resolve mixed node values correctly`() {
        val sharedStorage = NodeRepository {
            addEntry("bar1") {
                local(key("foo:3"), "Global Value 3")
            }
            addEntry("bar2") {
                local(key("foo:4"), "Global Value 4")
                local(key("foo:5"), "Global Value 5")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            local(key("foo:1"), "Value 1")
            local(key("foo:2"), "Value 2")
            composite(key("global:bar1"))
            composite(key("global:bar2"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5"), values)
    }

    @Test
    fun `should handle empty container without errors`() {
        val sharedStorage = NodeRepository<String>()
        val nodeContainer = NodeContainer(sharedStorage)

        val values = nodeContainer.values()
        assertTrue(values.isEmpty())
    }

    @Test
    fun `should correctly resolve single layer of nested nodes`() {
        val sharedStorage = NodeRepository {
            addEntry("nested1") {
                local(key("foo:nested1"), "Nested Value 1")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:nested1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Nested Value 1"), values)
    }

    @Test
    fun `should correctly resolve multiple layers of nested nodes`() {
        val sharedStorage = NodeRepository {
            addEntry("nested1") {
                composite(key("global:nested2"))
            }
            addEntry("nested2") {
                local(key("foo:nested2"), "Nested Value 2")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:nested1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Nested Value 2"), values)
    }

    @Test
    fun `should handle empty global data source without errors`() {
        val sharedStorage = NodeRepository<String>()
        val nodeContainer = NodeContainer(sharedStorage) { /* empty */ }

        val values = nodeContainer.values()
        assertTrue(values.isEmpty())
    }

    @Test
    fun `should include duplicate values from nested nodes with DSL`() {
        val sharedStorage = NodeRepository {
            addEntry("nested") {
                local(key("foo:nested"), "Duplicate Value")
                local(key("foo:nested"), "Duplicate Value")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:nested"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Duplicate Value", "Duplicate Value"), values)
    }

    @Test
    fun `should correctly resolve deeply nested nodes across multiple global keys`() {
        val sharedStorage = NodeRepository {
            addEntry("nested1") {
                composite(key("global:nested2"))
            }
            addEntry("nested2") {
                composite(key("global:nested3"))
            }
            addEntry("nested3") {
                local(key("foo:nested3"), "Deeply Nested Value 3")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:nested1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Deeply Nested Value 3"), values)
    }

    @Test
    fun `should correctly resolve single layer of nested composite nodes`() {
        val sharedStorage = NodeRepository {
            addEntry("layer1") {
                local(key("foo:layer1"), "Layer 1 Value")
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:layer1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 1 Value"), values)
    }

    @Test
    fun `should correctly resolve two layers of nested composite nodes`() {
        val sharedStorage = NodeRepository {
            addEntry("layer2") {
                local(key("foo:layer2"), "Layer 2 Value")
            }
            addEntry("layer1") {
                composite(key("global:layer2"))
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:layer1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 2 Value"), values)
    }

    @Test
    fun `should correctly resolve three layers of nested composite nodes`() {
        val sharedStorage = NodeRepository {
            addEntry("layer3") {
                local(key("foo:layer3"), "Layer 3 Value")
            }
            addEntry("layer2") {
                composite(key("global:layer3"))
            }
            addEntry("layer1") {
                composite(key("global:layer2"))
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:layer1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 3 Value"), values)
    }

    @Test
    fun `should correctly resolve four layers of nested composite nodes`() {
        val sharedStorage = NodeRepository {
            addEntry("layer4") {
                local(key("foo:layer4"), "Layer 4 Value")
            }
            addEntry("layer3") {
                composite(key("global:layer4"))
            }
            addEntry("layer2") {
                composite(key("global:layer3"))
            }
            addEntry("layer1") {
                local(key("foo:layer1"), "Layer 1 Value")
                composite(key("global:layer2"))
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:layer1"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 1 Value", "Layer 4 Value"), values)
    }

    /**
     * 测试循环引用是否会抛异常.
     *
     * 需要注意虽然循环引用不会抛异常, 但如果多个引用指向了同一个 LocalNode,
     * 那么最终 NodeContainer 中将包含重复的 LocalNode. 具体包含几个取决于
     * 有几个引用指向了这个 LocalNode.
     */
    @Test
    fun `should not throw exception when resolving circular references`() {
        val sharedStorage = NodeRepository {
            addEntry("layer1") {
                local(key("foo:layer1"), "Layer 1 Value")
                composite(key("global:layer2"))
            }
            addEntry("layer2") {
                composite(key("global:layer3"))
            }
            addEntry("layer3") {
                composite(key("global:layer1"))
            }
        }

        val nodeContainer = NodeContainer(sharedStorage) {
            composite(key("global:layer1"))
            composite(key("global:layer2"))
            composite(key("global:layer3"))
        }

        val values = nodeContainer.values()
        assertEquals(listOf("Layer 1 Value", "Layer 1 Value", "Layer 1 Value"), values)
    }

}