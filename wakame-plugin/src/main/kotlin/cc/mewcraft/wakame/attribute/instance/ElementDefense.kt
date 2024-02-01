package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.item.Tang
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 元素防御力
 */
class ElementDefense : KoinComponent {
    private val key = Key.key(Tang.ATTRIBUTE_NAMESPACE, "element_defense")
}