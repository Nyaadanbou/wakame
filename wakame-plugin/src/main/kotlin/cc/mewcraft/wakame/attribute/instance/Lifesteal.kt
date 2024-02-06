package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 攻击吸血
 */
class Lifesteal : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "lifesteal")
}