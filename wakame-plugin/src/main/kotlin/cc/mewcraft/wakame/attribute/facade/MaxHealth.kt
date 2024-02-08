package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 最大生命值
 */
class MaxHealth : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "max_health")
}