package cc.mewcraft.wakame.item.schema.cell.core.skill

import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.ConfiguredSkillWithTrigger
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode

/**
 * A constructor function to create [SchemaSkillCore].
 *
 * @return a new instance of [SchemaSkillCore]
 */
fun SchemaSkillCore(node: ConfigurationNode): SchemaSkillCore {
    val configuredSkillWithTrigger = node.krequire<ConfiguredSkillWithTrigger>()
    val key = configuredSkillWithTrigger.key
    val trigger = configuredSkillWithTrigger.trigger
    val schemaSkillCore = SchemaSkillCoreImpl(key, trigger)
    return schemaSkillCore
}

/**
 * Represents a [SchemaCore] of a skill.
 */
interface SchemaSkillCore : SchemaCore {
    val instance: ConfiguredSkill
    val trigger: SkillTrigger
    override fun reify(context: SchemaGenerationContext): BinarySkillCore
}

/* Some useful extension functions */
