package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.schema.cell.core.attribute.SchemaAttributeCore
import cc.mewcraft.wakame.item.schema.cell.core.skill.SchemaSkillCore
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A factory used to create [SchemaCore].
 */
object SchemaCoreFactory {
    fun empty(): SchemaCore {
        TODO("实现空的词条栏核心（可用于给空词条栏进行分类）1")
    }

    /**
     * 从配置文件创建一个 [SchemaCore].
     */
    fun schemaOf(node: ConfigurationNode): SchemaCore {
        val key = node.node("key").krequire<Key>()
        val ret: SchemaCore = when (key.namespace()) {
            Namespaces.SKILL -> SchemaSkillCore(node)
            Namespaces.ATTRIBUTE -> SchemaAttributeCore(node)
            else -> throw IllegalArgumentException("Unknown key namespace for schema core: ${key.namespace()}")
        }

        return ret
    }
}