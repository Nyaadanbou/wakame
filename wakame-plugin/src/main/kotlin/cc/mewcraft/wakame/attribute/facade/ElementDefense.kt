package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 元素防御力
 */
class ElementDefense : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "element_defense")
}