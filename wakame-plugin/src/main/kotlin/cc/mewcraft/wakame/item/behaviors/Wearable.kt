package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory

/**
 * 物品穿戴的逻辑.
 */
interface Wearable : ItemBehavior {
    private object Default : Wearable

    companion object : ItemBehaviorFactory<Wearable> {
        override fun create(): Wearable {
            return Default
        }
    }
}