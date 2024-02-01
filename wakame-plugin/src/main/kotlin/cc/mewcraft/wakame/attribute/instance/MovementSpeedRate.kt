package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 移动速度 %
 */
class MovementSpeedRate : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "movement_speed_rate")
}