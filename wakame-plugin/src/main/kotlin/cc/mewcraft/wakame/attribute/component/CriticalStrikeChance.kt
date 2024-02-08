package cc.mewcraft.wakame.attribute.component

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 暴击概率 %
 */
class CriticalStrikeChance : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "critical_strike_chance")
}