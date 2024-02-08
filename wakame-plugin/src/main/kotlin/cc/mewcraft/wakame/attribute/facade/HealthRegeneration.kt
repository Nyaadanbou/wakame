package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 生命回复
 */
class HealthRegeneration : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "health_regeneration")
}