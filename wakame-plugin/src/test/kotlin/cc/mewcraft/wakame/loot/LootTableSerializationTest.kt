package cc.mewcraft.wakame.loot

import cc.mewcraft.wakame.loot.context.LootContext
import cc.mewcraft.wakame.loot.entry.ComposableEntryContainer
import cc.mewcraft.wakame.loot.predicate.LootPredicate
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.test.Test
import kotlin.test.assertEquals

class LootTableSerializationTest {

    private fun createLoaderBuilder(): YamlConfigurationLoader.Builder {
        return YamlConfigurationLoader.builder()
            .defaultOptions { option ->
                option.serializers {
                    it.register(LootTable.SERIALIZER)
                    it.register(LootPool.SERIALIZER)
                    it.register(LootPredicate.SERIALIZER)
                    it.register(ComposableEntryContainer.SERIALIZER)
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
        val selected = deserialized.select(LootContext.EMPTY)
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
        val selected = deserialized.select(LootContext.EMPTY)
        println("Selected from alternatives: $selected")
        assertEquals(selected.size, 1, "Expected one entry to be selected from alternatives, got ${selected.size}")
    }
}