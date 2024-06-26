package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory

/**
 * 物品附魔的逻辑.
 */
interface Enchantable : ItemBehavior {
    private object Default : Enchantable

    companion object : ItemBehaviorFactory<Enchantable> {
        override fun create(): Enchantable {
            return Default
        }
    }
}