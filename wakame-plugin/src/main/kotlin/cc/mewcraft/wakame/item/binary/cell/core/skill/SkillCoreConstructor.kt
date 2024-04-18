package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.item.SkillTrigger
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
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
    val key = node.node("key").krequire<Key>()
    val trigger = node.node("trigger").krequire<SkillTrigger>()
    return BinarySkillCoreDataHolder(key, trigger)
}
