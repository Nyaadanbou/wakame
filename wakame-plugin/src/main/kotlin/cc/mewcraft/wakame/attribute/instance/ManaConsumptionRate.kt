package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 魔法消耗倍率 %
 */
class ManaConsumptionRate : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "mana_consumption_rate")
}