package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A factory used to create [SchemaCore].
 */
object SchemaCoreFactory {
    fun empty(): SchemaCore {
        TODO("实现空的词条栏核心（可用于给空词条栏进行分类）")
    }

    /**
     * 由配置文件调用，因此已知：
     * - [ConfigurationNode]
     *
     * 由此又可以读取到：
     * - Namespace
     * - Value
     *
     * 最后通过 Namespace + Value 我们可以唯一确定用什么实现来反序列化该 [ConfigurationNode].
     */
    fun schemaOf(node: ConfigurationNode): SchemaCore {
        val key = node.node("key").krequire<Key>()
        val ret = when (key.namespace()) {
            NekoNamespaces.SKILL -> {
                SchemaSkillCore(key)
            }

            NekoNamespaces.ATTRIBUTE -> {
                val schemaEncoder = AttributeRegistry.FACADES[key].SCHEMA_DATA_NODE_ENCODER
                val schemaData = schemaEncoder.encode(node)
                SchemaAttributeCore(key, schemaData)
            }

            else -> throw IllegalArgumentException()
        }

        return ret
    }
}