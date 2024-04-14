package cc.mewcraft.wakame.item.schema.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.BinarySkillCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

/**
 * A constructor function to create [SchemaSkillCore].
 *
 * @return a new instance of [SchemaSkillCore]
 */
fun SchemaSkillCore(node: ConfigurationNode): SchemaSkillCore {
    val key = node.node("key").krequire<Key>()
    val schemaSkillCore = SchemaSkillCoreSimple(key)
    return schemaSkillCore
}

/**
 * Represents a [SchemaCore] of a skill.
 */
interface SchemaSkillCore : SchemaCore {
    override val key: Key
    override fun generate(context: SchemaGenerationContext): BinarySkillCore
}