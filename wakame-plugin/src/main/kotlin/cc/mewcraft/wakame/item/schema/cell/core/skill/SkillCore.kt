package cc.mewcraft.wakame.item.schema.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.trigger.SkillWithTrigger
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode

/**
 * A constructor function to create [SchemaSkillCore].
 *
 * @return a new instance of [SchemaSkillCore]
 */
fun SchemaSkillCore(node: ConfigurationNode): SchemaSkillCore {
    val skillWithTrigger = node.krequire<SkillWithTrigger>()
    val key = skillWithTrigger.key
    val trigger = skillWithTrigger.trigger
    val schemaSkillCore = SchemaSkillCoreImpl(key, trigger)
    return schemaSkillCore
}

/**
 * Represents a [SchemaCore] of a skill.
 */
interface SchemaSkillCore : SchemaCore {
    val instance: Skill
    val trigger: Trigger
    override fun reify(context: SchemaGenerationContext): BinarySkillCore
}

/* Some useful extension functions */
