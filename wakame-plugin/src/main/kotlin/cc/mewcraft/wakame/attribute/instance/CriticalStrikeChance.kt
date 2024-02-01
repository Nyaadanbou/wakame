package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 暴击概率 %
 */
class CriticalStrikeChance : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "critical_strike_chance")
}