package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory

/**
 * 物品提供属性加成的逻辑.
 */
interface Attributable : ItemBehavior {
    private object Default : Attributable

    companion object : ItemBehaviorFactory<Attributable> {
        override fun create(): Attributable {
            return Default
        }
    }
}