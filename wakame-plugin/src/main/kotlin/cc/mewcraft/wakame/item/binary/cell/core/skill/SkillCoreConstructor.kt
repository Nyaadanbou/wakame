package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.skill.ConfiguredSkillWithTrigger
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
    return BinarySkillCoreNBTWrapper(compound)
}

/**
 * A constructor function to create [BinarySkillCore].
 */
fun BinarySkillCore(
    node: ConfigurationNode,
): BinarySkillCore {
    val configuredSkillWithTrigger = node.krequire<ConfiguredSkillWithTrigger>()
    val key = configuredSkillWithTrigger.key
    val trigger = configuredSkillWithTrigger.trigger
    return BinarySkillCoreDataHolder(key, trigger)
}
