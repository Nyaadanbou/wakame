package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 魔法消耗倍率 %
 */
class ManaConsumptionRate : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "mana_consumption_rate")
}