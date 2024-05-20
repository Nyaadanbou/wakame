package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.skill.SkillWithTrigger
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
    val skillWithTrigger = node.krequire<SkillWithTrigger>()
    val key = skillWithTrigger.key
    val trigger = skillWithTrigger.trigger
    return BinarySkillCoreDataHolder(key, trigger)
}
