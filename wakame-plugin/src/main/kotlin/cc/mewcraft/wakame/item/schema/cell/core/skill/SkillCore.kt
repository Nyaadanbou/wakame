package cc.mewcraft.wakame.item.schema.cell.core.skill

import cc.mewcraft.wakame.item.SkillInstance
import cc.mewcraft.wakame.item.SkillTrigger
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
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
    val trigger = node.node("trigger").krequire<SkillTrigger>()
    val schemaSkillCore = SchemaSkillCoreImpl(key, trigger)
    return schemaSkillCore
}

/**
 * Represents a [SchemaCore] of a skill.
 */
interface SchemaSkillCore : SchemaCore {
    val instance: SkillInstance
    val trigger: SkillTrigger
    override fun reify(context: SchemaGenerationContext): BinarySkillCore
}

/* Some useful extension functions */
