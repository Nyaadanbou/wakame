package cc.mewcraft.wakame.attribute.component

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 最终承伤倍率 %
 */
class DamageTakenRate : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "damage_taken_rate")
}