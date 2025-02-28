package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

interface Wearable : ItemBehavior {
    private object Default : Wearable

    companion object Type : ItemBehaviorType<Wearable> {
        override fun create(): Wearable = Default
    }
}