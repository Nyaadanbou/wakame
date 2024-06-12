package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.skill.trigger.ConfiguredSkill
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.spongepowered.configurate.ConfigurationNode

//
// 构造函数，用于创建对象
//

/**
 * A constructor function to create [BinarySkillCore].
 */
fun BinarySkillCore(compound: CompoundShadowTag): BinarySkillCore {
    return BinarySkillCoreTagWrapper(compound)
}

/**
 * A constructor function to create [BinarySkillCore].
 */
fun BinarySkillCore(
    node: ConfigurationNode,
): BinarySkillCore {
    val configuredSkill = node.krequire<ConfiguredSkill>()
    val key = configuredSkill.key
    val trigger = configuredSkill.trigger
    val effectiveVariant = configuredSkill.effectiveVariant
    return BinarySkillCoreDataHolder(key, trigger, effectiveVariant)
}
