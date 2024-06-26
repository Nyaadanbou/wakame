package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorFactory

/**
 * 物品作为工具的逻辑.
 */
interface Tool : ItemBehavior {
    private object Default : Tool {}

    companion object Factory : ItemBehaviorFactory<Tool> {
        override fun create(): Tool {
            return Default
        }
    }
}