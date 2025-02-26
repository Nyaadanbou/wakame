package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

interface Tool : ItemBehavior {
    private object Default : Tool

    companion object Type : ItemBehaviorType<Tool> {
        override fun create(): Tool = Default
    }
}