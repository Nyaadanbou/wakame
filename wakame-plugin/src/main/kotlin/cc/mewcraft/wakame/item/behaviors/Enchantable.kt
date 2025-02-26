package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

interface Enchantable : ItemBehavior {
    private object Default : Enchantable

    companion object Type : ItemBehaviorType<Enchantable> {
        override fun create(): Enchantable = Default
    }
}