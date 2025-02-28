package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

/**
 * 物品进行充能的逻辑.
 */
interface Chargeable : ItemBehavior {
    private object Default : Chargeable

    companion object Type : ItemBehaviorType<Chargeable> {
        override fun create(): Chargeable = Default
    }
}