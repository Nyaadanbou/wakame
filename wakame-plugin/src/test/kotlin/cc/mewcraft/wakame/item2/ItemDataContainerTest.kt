package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.transformation.Transformations
import cc.mewcraft.wakame.transformation.transforms.KoishTransformation
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.test.TestOnly
import cc.mewcraft.wakame.util.yamlLoader
import org.junit.jupiter.api.BeforeAll
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemDataContainerTest {
    companion object {
        @OptIn(TestOnly::class)
        @JvmStatic
        @BeforeAll
        fun setup() {
            Transformations.addTransform(TransformTest92213530)
        }
    }

    @Test
    fun `test item data serialization`() {
        val loader = createLoaderBuilder()
        val rootNode = loader.buildAndLoadString(
            """
            id: "example/bronze_helmet"
            variant: 1919810
            version: 0 # 模拟旧版本数据
            """.trimIndent()
        )

        val itemData = rootNode.require<ItemDataContainer>()
        assertEquals(Identifiers.of("example/bronze_helmet"), itemData[ItemDataTypes.ID]?.id)
        assertEquals(92213530, itemData[ItemDataTypes.VERSION])
        assertEquals(114514, itemData[ItemDataTypes.VARIANT])
    }

    private object TransformTest92213530 : KoishTransformation(92213530) {
        override fun apply(node: ConfigurationNode) {
            // 模拟旧版本数据转换
            val data = node.data(ItemDataTypes.VARIANT)
            assertEquals(1919810, data)

            node.dataNode(ItemDataTypes.VARIANT).set(114514)
        }
    }

    private fun createLoaderBuilder(): YamlConfigurationLoader.Builder {
        return yamlLoader {
            withDefaults()

            serializers {
                registerAll(ItemDataContainer.makeDirectSerializers())
            }
        }
    }
}