package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.entry.ComposableEntryContainer
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.test.Test
import kotlin.test.assertEquals

class LootTableSerializationTest {

    private fun createLoaderBuilder(): YamlConfigurationLoader.Builder {
        return yamlLoader {
            withDefaults()
            serializers {
                register(LootTable.SERIALIZER)
                register(LootPool.SERIALIZER)
                register(LootPredicate.SERIALIZER)
                register(ComposableEntryContainer.SERIALIZER)
            }
        }
    }

    @Test
    fun `test loot table serialization`() {
        val loader = createLoaderBuilder()
        val rootNode = loader.buildAndLoadString(
            """
            pools:
              - rolls: 1
                entries:
                  - type: simple
                    data: 12
                    weight: 1
            """.trimIndent()
        )

        val deserialized = rootNode.require<LootTable<Int>>()
        println("Deserialized: $deserialized")
        assertEquals(deserialized.pools.size, 1, "Expected one pool, got ${deserialized.pools.size}")
        val selected = deserialized.select(LootContext.default())
        println("Selected: $selected")
        assertEquals(selected.size, 1, "Expected one entry to be selected, got ${selected.size}")
        assertEquals(selected[0], 12, "Expected selected entry data to be 12, got ${selected[0]}")
    }

    @Test
    fun `test loot table serialization with composite entries`() {
        val loader = createLoaderBuilder()
        val rootNode = loader.buildAndLoadString(
            """
            pools:
              - rolls: 1
                entries:
                  - type: alternatives
                    children:
                      - type: simple
                        data: 12
                        weight: 1
                      - type: simple
                        data: 24
                        weight: 1
            """.trimIndent()
        )

        val deserialized = rootNode.require<LootTable<Int>>()
        println("Deserialized with alternatives: $deserialized")
        val selected = deserialized.select(LootContext.default())
        println("Selected from alternatives: $selected")
        assertEquals(selected.size, 1, "Expected one entry to be selected from alternatives, got ${selected.size}")
    }

    @ConfigSerializable
    data class TestData(
        val intValue: Int,
        val doubleValue: Double,
        val stringValue: String,
        val booleanValue: Boolean,
        val listValue: List<Int>,
        val mapValue: Map<String, String>,
    )

    @Test
    fun `test loot table serialization with complex data`() {
        val loader = createLoaderBuilder()
        val rootNode = loader.buildAndLoadString(
            """
            pools:
              - rolls: 1
                entries:
                  - type: simple
                    data: 
                      int_value: 42
                      double_value: 3.14
                      string_value: "Hello, World!"
                      boolean_value: true
                      list_value: [1, 2, 3]
                      map_value: {key1: value1, key2: value2}
                    weight: 1
            """.trimIndent()
        )

        val deserialized = rootNode.require<LootTable<TestData>>()
        println("Deserialized complex data: $deserialized")
        val selected = deserialized.select(LootContext.default())
        println("Selected complex data: $selected")
        assertEquals(selected.size, 1, "Expected one entry to be selected, got ${selected.size}")
        assertEquals(selected[0].intValue, 42, "Expected intValue to be 42, got ${selected[0].intValue}")
        assertEquals(selected[0].doubleValue, 3.14, "Expected doubleValue to be 3.14, got ${selected[0].doubleValue}")
        assertEquals(selected[0].stringValue, "Hello, World!", "Expected stringValue to be 'Hello, World!', got ${selected[0].stringValue}")
        assertEquals(selected[0].booleanValue, true, "Expected booleanValue to be true, got ${selected[0].booleanValue}")
        assertEquals(selected[0].listValue, listOf(1, 2, 3), "Expected listValue to be [1, 2, 3], got ${selected[0].listValue}")
        assertEquals(selected[0].mapValue, mapOf("key1" to "value1", "key2" to "value2"), "Expected mapValue to be {key1: value1, key2: value2}, got ${selected[0].mapValue}")
    }
}
