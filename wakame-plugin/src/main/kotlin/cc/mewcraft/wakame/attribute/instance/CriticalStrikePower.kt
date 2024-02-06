package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 暴击伤害倍率 %
 */
class CriticalStrikePower : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "critical_strike_power")
}