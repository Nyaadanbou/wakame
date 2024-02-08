package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 魔法汲取率 %
 */
class ManastealRate : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "manasteal_rate")
}