package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 生命回复
 */
class HealthRegeneration : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "health_regeneration")
}