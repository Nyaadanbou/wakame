package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

interface Castable : ItemBehavior {
    private object Default : Castable

    companion object Type : ItemBehaviorType<Castable> {
        override fun create(): Castable {
            return Default
        }
    }
}