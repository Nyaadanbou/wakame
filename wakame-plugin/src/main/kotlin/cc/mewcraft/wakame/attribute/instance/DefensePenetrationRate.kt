package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 防御穿透率 %
 */
class DefensePenetrationRate : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "defense_penetration_rate")
}