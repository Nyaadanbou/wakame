package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode

//
// 构造函数，用于创建对象
//

/**
 * A constructor function to create [BinarySkillCore].
 */
fun BinarySkillCore(compound: CompoundTag): BinarySkillCore {
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
    val effectiveVariant = configuredSkill.variant
    return BinarySkillCoreDataHolder(key, trigger, effectiveVariant)
}
