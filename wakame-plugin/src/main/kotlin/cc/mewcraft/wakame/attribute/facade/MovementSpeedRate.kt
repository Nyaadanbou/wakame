package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 移动速度 %
 */
class MovementSpeedRate : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "movement_speed_rate")
}