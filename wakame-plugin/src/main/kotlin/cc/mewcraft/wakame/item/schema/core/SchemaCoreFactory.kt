package cc.mewcraft.wakame.item.schema.core

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.requireKt
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A factory used to create [SchemaCore].
 */
object SchemaCoreFactory {

    /**
     * 由配置文件调用，因此已知：
     * - [ConfigurationNode]
     *
     * 由此已知：
     * - Namespace
     * - Value
     *
     * 通过 Namespace + Value 我们可以唯一确定用什么实现来反序列化该 [ConfigurationNode].
     */
    fun schemaOf(node: ConfigurationNode): SchemaCore {
        val key = node.node("key").requireKt<Key>()
        val ret = when (key.namespace()) {
            NekoNamespaces.ABILITY -> {
                SchemaAbilityCore(key)
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