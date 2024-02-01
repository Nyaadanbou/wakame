package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 最大护盾
 */
class MaxAbsorption : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "max_absorption")
}