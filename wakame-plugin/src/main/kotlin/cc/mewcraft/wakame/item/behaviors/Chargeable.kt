package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory

/**
 * 物品进行充能的逻辑.
 */
interface Chargeable : ItemBehavior {
    private class Default : Chargeable

    companion object : ItemBehaviorFactory<Chargeable> {
        override fun create(): Chargeable {
            return Default()
        }
    }
}