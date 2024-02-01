package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 魔法汲取率 %
 */
class ManastealRate : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "manasteal_rate")
}