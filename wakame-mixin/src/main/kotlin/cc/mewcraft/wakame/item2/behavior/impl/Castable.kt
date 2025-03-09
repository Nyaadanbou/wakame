package cc.mewcraft.wakame.item2.behavior.impl

import cc.mewcraft.wakame.item2.behavior.ItemBehavior
import cc.mewcraft.wakame.item2.behavior.ItemBehaviorType

interface Castable : ItemBehavior {
    private object Default : Castable

    companion object Type : ItemBehaviorType<Castable> {
        override fun create(): Castable = Default
    }
}