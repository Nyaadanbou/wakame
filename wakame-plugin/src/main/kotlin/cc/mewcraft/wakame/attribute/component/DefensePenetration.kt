package cc.mewcraft.wakame.attribute.component

import cc.mewcraft.wakame.item.Core
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent

/**
 * 防御穿透
 */
class DefensePenetration : KoinComponent {
    private val key = Key.key(Core.ATTRIBUTE_NAMESPACE, "defense_penetration")
}