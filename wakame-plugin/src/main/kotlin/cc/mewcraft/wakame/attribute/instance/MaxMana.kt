package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 最大魔法值
 */
class MaxMana : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "max_mana")
}