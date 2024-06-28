package cc.mewcraft.wakame.item.behaviors

import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.ItemBehaviorType

/**
 * 物品作为工具的逻辑.
 */
interface Tool : ItemBehavior {
    private object Default : Tool {}

    companion object Type : ItemBehaviorType<Tool> {
        override fun create(): Tool {
            return Default
        }
    }
}