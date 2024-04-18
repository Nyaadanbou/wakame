package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.item.schema.cell.core.attribute.SchemaAttributeCore
import cc.mewcraft.wakame.item.schema.cell.core.empty.SchemaEmptyCore
import cc.mewcraft.wakame.item.schema.cell.core.noop.SchemaNoopCore
import cc.mewcraft.wakame.item.schema.cell.core.skill.SchemaSkillCore
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A factory used to create [SchemaCore].
 */
object SchemaCoreFactory {

    /**
     * 从配置文件创建一个 [SchemaCore].
     */
    fun create(node: ConfigurationNode): SchemaCore {
        val key = node.node("key").krequire<Key>()

        val ret = when {
            key == GenericKeys.NOOP -> SchemaNoopCore()
            key == GenericKeys.EMPTY -> SchemaEmptyCore()
            key.namespace() == Namespaces.SKILL -> SchemaSkillCore(node)
            key.namespace() == Namespaces.ATTRIBUTE -> SchemaAttributeCore(node)
            else -> throw IllegalArgumentException("Unknown key namespace for schema core: ${key.namespace()}")
        }

        return ret
    }
}